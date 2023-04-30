package io.orangebuffalo.testcontainers.playwright

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
    private val json = Json { ignoreUnknownKeys = true }
    private var chromiumWsEndpoint: String? = null
    private var firefoxWsEndpoint: String? = null
    private var webkitWsEndpoint: String? = null
    private val apiManager = PlaywrightApiManager(this)

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

    fun getPlaywrightContainerApi(): PlaywrightApi = apiManager.getPlaywrightApi()

    override fun close() {
        apiManager.close()
        super.close()
    }

    @Synchronized
    internal fun getChromiumWsEndpoint(): String {
        if (chromiumWsEndpoint == null) {
            chromiumWsEndpoint = createBrowserAndGetWsEndpoint(
                ContainerApi.LaunchRequest(
                    browser = ContainerApi.PlaywrightBrowserType.CHROMIUM,
                    port = chromiumPort
                )
            )
        }
        return chromiumWsEndpoint!!
    }

    @Synchronized
    internal fun getFirefoxWsEndpoint(): String {
        if (firefoxWsEndpoint == null) {
            firefoxWsEndpoint = createBrowserAndGetWsEndpoint(
                ContainerApi.LaunchRequest(
                    browser = ContainerApi.PlaywrightBrowserType.FIREFOX,
                    port = firefoxPort
                )
            )
        }
        return firefoxWsEndpoint!!
    }

    @Synchronized
    internal fun getWebkitWsEndpoint(): String {
        if (webkitWsEndpoint == null) {
            webkitWsEndpoint = createBrowserAndGetWsEndpoint(
                ContainerApi.LaunchRequest(
                    browser = ContainerApi.PlaywrightBrowserType.WEBKIT,
                    port = webkitPort
                )
            )
        }
        return webkitWsEndpoint!!
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
