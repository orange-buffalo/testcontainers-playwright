package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.Selectors
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock

private val log = mu.KotlinLogging.logger {}
private const val apiPort = 3000
private const val chromiumPort = 4444
private const val firefoxPort = 4445
private const val webkitPort = 4446

class PlaywrightContainer(private val customImage: DockerImageName? = null) :
    GenericContainer<PlaywrightContainer>(DockerImageName.parse("ghcr.io/orange-buffalo/testcontainers-playwright:1.31")) {

    private val playwrightVersion = getPlaywrightVersionOnClasspath()
    private val playwrightVersionCompatibleTag = playwrightVersion?.split(".")?.let { parts ->
        if (parts.size >= 2) {
            "${parts[0]}.${parts[1]}"
        } else {
            playwrightVersion
        }
    }
    private val playwrightInstances = CopyOnWriteArrayList<Playwright>()
    private val json = Json { ignoreUnknownKeys = true }
    private val chromiumAccess = ContainerBrowserAccess(
        browser = ContainerApi.PlaywrightBrowserType.CHROMIUM, port = chromiumPort
    )
    private val firefoxAccess = ContainerBrowserAccess(
        browser = ContainerApi.PlaywrightBrowserType.FIREFOX, port = firefoxPort
    )
    private val webkitAccess = ContainerBrowserAccess(
        browser = ContainerApi.PlaywrightBrowserType.WEBKIT, port = webkitPort
    )

    override fun configure() {
        if (playwrightVersion == null && customImage == null) {
            throw IllegalArgumentException(
                "Playwright version could not be detected on classpath " +
                        "and image was not specified. Please ensure Playwright is present on classpath " +
                        "or provide a particular image of testcontainers-playwright Docker image that is " +
                        "compatible with your code."
            )
        }

        withCopyToContainer(MountableFile.forClasspathResource("app.js"), "/app/app.js")

        if (customImage == null) {
            log.info { "Will use Playwright image $playwrightVersionCompatibleTag for version $playwrightVersion found on classpath" }
            super.setDockerImageName("ghcr.io/orange-buffalo/testcontainers-playwright:$playwrightVersionCompatibleTag")
        }

        withExposedPorts(apiPort, chromiumPort, firefoxPort, webkitPort)
        waitingFor(LogMessageWaitStrategy().withRegEx("Server running at .*"))
    }

    override fun start() {
        super.start()

        val versionInContainer = try {
            copyFileFromContainer("/app/version.txt") { stream ->
                stream.reader().toString()
            }
        } catch (e: Exception) {
            log.warn(e) { "Failed to determine Playwright version in container" }
            null
        }

        if (customImage != null && versionInContainer != playwrightVersionCompatibleTag) {
            log.warn {
                "Playwright version in container ($versionInContainer) is not compatible with " +
                        "Playwright on the classpath ($playwrightVersion). This might lead to unexpected results, " +
                        "for instance browser being started locally instead of in container."
            }
        }
    }

    fun registerNewPlaywright(): PlaywrightContainerApi {
        val playwright = Playwright.create()
        playwrightInstances.add(playwright)
        return PlaywrightContainerApiImp(playwright)
    }

    override fun close() {
        playwrightInstances.forEach {
            try {
                it.close()
            } catch (e: PlaywrightException) {
                log.warn(e) { "Failed to close Playwright instance" }
            }
        }
        super.close()
    }

    private inner class PlaywrightContainerApiImp(
        private val playwright: Playwright
    ) : PlaywrightContainerApi {
        private var chromiumBrowser: Browser? = null
        private var firefoxBrowser: Browser? = null
        private var webkitBrowser: Browser? = null

        override fun chromium(): Browser {
            if (chromiumBrowser == null) {
                chromiumBrowser = chromiumAccess.safeGetBrowser { wsEndpoint ->
                    playwright.chromium().connect(wsEndpoint)
                }
            }
            return chromiumBrowser!!
        }

        override fun firefox(): Browser {
            if (firefoxBrowser == null) {
                firefoxBrowser = firefoxAccess.safeGetBrowser { wsEndpoint ->
                    playwright.firefox().connect(wsEndpoint)
                }
            }
            return firefoxBrowser!!
        }

        override fun webkit(): Browser {
            if (webkitBrowser == null) {
                webkitBrowser = webkitAccess.safeGetBrowser { wsEndpoint ->
                    playwright.webkit().connect(wsEndpoint)
                }
            }
            return webkitBrowser!!
        }

        override fun selectors(): Selectors = playwright.selectors()
    }

    /**
     * Ensures a single instance of browser is stared in the container.
     */
    private inner class ContainerBrowserAccess(
        private val browser: ContainerApi.PlaywrightBrowserType,
        private val port: Int,
    ) {
        private val lock = ReentrantLock()
        private var wsEndpoint: String? = null

        fun safeGetBrowser(spec: (wsEndpoint: String) -> Browser): Browser {
            lock.lock()
            try {
                if (wsEndpoint != null) {
                    return spec(wsEndpoint!!)
                }
                wsEndpoint = createBrowserAndGetWsEndpoint(
                    ContainerApi.LaunchRequest(
                        browser = browser,
                        port = port,
                    )
                )
                return spec(wsEndpoint!!)
            } finally {
                lock.unlock()
            }
        }

        @Suppress("HttpUrlsUsage")
        private fun createBrowserAndGetWsEndpoint(params: ContainerApi.LaunchRequest): String {
            log.debug { "Launching a new browser in container: $params" }

            val httpClient = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://${host}:${getMappedPort(apiPort)}/launch"))
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        json.encodeToString(ContainerApi.LaunchRequest.serializer(), params)
                    )
                )
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                throw IllegalStateException(
                    "Failed to launch browser in container: ${response.statusCode()} / ${
                        response.headers().map()
                    } / [${response.body()}]"
                )
            }
            val responseBody = response.body()
            log.debug { "Got response from the container: $responseBody" }

            val jsonResponse = json.decodeFromString(ContainerApi.LaunchResponse.serializer(), responseBody)
            val wsPath = jsonResponse.wsPath

            val wsEndpoint = "ws://$host:${getMappedPort(params.port)}/$wsPath"
            log.debug { "Will connect to $wsPath ($wsEndpoint)" }

            return wsEndpoint
        }
    }
}

private class ContainerApi {
    @Serializable
    data class LaunchResponse(val wsPath: String)

    enum class PlaywrightBrowserType {
        CHROMIUM,
        FIREFOX,
        WEBKIT
    }

    @Serializable
    data class LaunchRequest(
        val browser: PlaywrightBrowserType,
        val port: Int,
    )
}
