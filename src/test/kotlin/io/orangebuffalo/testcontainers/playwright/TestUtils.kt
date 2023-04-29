package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain
import org.testcontainers.containers.Network
import org.testcontainers.containers.NginxContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.utility.MountableFile

internal fun Page.navigateAndVerify(): Page {
    navigate("http://web/")
    title().shouldContain("Playwright Testcontainers Page")
    return this
}

internal fun Browser.openAndVerifyTestPage(): Page = newPage().navigateAndVerify()

internal fun Page.shouldRunInChromium() {
    val isChromium = evaluate("navigator.userAgent.includes('HeadlessChrome')") as Boolean
    isChromium.shouldBeTrue()
}

internal fun Page.shouldRunInFirefox() {
    val isFirefox = evaluate("navigator.userAgent.includes('Firefox')") as Boolean
    isFirefox.shouldBeTrue()
}

internal fun Page.shouldRunInWebkit() {
    val isWebKit = evaluate("!navigator.userAgent.includes('HeadlessChrome') && !navigator.userAgent.includes('Firefox')") as Boolean
    isWebKit.shouldBeTrue()
}

private val testNetwork = Network.newNetwork()

private val nginx = NginxContainer("nginx")
    .withCopyToContainer(MountableFile.forClasspathResource("/html"), "/usr/share/nginx/html")
    .waitingFor(HttpWaitStrategy())
    .withNetwork(testNetwork)
    .withNetworkAliases("web")

internal fun PlaywrightContainer.connectToWebServer() {
    withNetwork(testNetwork)
    dependsOn(nginx)
}
