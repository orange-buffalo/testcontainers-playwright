package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CopyOnWriteArrayList

private val log = mu.KotlinLogging.logger {}

class PlaywrightContainer(private val customImage: DockerImageName? = null) :
    GenericContainer<PlaywrightContainer>(DockerImageName.parse("mcr.microsoft.com/playwright:focal")) {

    private val playwrightVersion = getPlaywrightVersionOnClasspath()
    private val playwrightVersionCompatibleTag = playwrightVersion?.split(".")?.let { parts ->
        if (parts.size >= 2) {
            "${parts[0]}.${parts[1]}"
        } else {
            playwrightVersion
        }
    }
    private val playwrightInstances = CopyOnWriteArrayList<Playwright>()

    init {
        if (playwrightVersion == null && customImage == null) {
            throw IllegalArgumentException(
                "Playwright version could not be detected on classpath " +
                        "and image was not specified. Please ensure Playwright is present on classpath " +
                        "or provide a particular image of testcontainers-playwright Docker image that is " +
                        "compatible with your code."
            )
        }

        if (customImage != null
            && playwrightVersionCompatibleTag != null
            && customImage.versionPart != playwrightVersionCompatibleTag
        ) {
            log.warn {
                "Playwright version detected on classpath ($playwrightVersion) is not compatible with " +
                        "the provided image ($customImage). This might lead to unexpected results, for instance" +
                        "browser being started locally instead of in container."
            }
        }
    }

    override fun configure() {
        withExposedPorts(4444)
        withExposedPorts(3000)
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
            .uri(URI.create("http://${host}:${getMappedPort(3000)}/v1/launch"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get()
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
