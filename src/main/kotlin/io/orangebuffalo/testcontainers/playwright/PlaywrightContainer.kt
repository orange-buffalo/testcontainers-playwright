package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CopyOnWriteArrayList

private val log = mu.KotlinLogging.logger {}

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

        withExposedPorts(3000, 4444)
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

    fun registerNewBrowser(): Browser {
        val playwright = Playwright.create()
        playwrightInstances.add(playwright)
        return playwright.chromium().connect(createBrowserAndGetWsEndpoint())
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

    @Suppress("HttpUrlsUsage")
    private fun createBrowserAndGetWsEndpoint(): String {
        log.debug { "Launching a new browser in container" }

        val httpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://${host}:${getMappedPort(3000)}/launch"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get()
        if (response.statusCode() != 200) {
            throw IllegalStateException(
                "Failed to launch browser in container: ${response.statusCode()} / ${
                    response.headers().map()
                } / [${response.body()}]"
            )
        }
        val responseBody = response.body()
        log.debug { "Got response from the container: $responseBody" }

        val jsonDecoder = Json { ignoreUnknownKeys = true }
        val jsonResponse = jsonDecoder.decodeFromString(ContainerApiV1Response.serializer(), responseBody)
        val wsPath = jsonResponse.wsPath

        val wsEndpoint = "ws://$host:${getMappedPort(4444)}/$wsPath"
        log.debug { "Will connect to $wsPath ($wsEndpoint)" }

        return wsEndpoint
    }
}

@Serializable
private data class ContainerApiV1Response(val wsPath: String)
