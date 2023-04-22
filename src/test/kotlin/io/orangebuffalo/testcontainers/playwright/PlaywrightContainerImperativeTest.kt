package io.orangebuffalo.testcontainers.playwright

import mu.KotlinLogging
import kotlin.test.Test

var log = KotlinLogging.logger {}

internal class PlaywrightContainerImperativeTest {

    @Test
    fun shouldProvideValidChromiumBrowser() = withContainer {
        registerNewPlaywright()
            .chromium()
            .openAndVerifyTestPage()
            .shouldRunInChromium()
    }

    @Test
    fun shouldProvideValidFirefoxBrowser() = withContainer {
        registerNewPlaywright()
            .firefox()
            .openAndVerifyTestPage()
            .shouldRunInFirefox()
    }

    @Test
    fun shouldProvideValidWebkitBrowser() = withContainer {
        registerNewPlaywright()
            .webkit()
            .openAndVerifyTestPage()
            .shouldRunInWebkit()
    }

    private fun withContainer(block: PlaywrightContainer.() -> Unit) {
        PlaywrightContainer().use { container ->
            container.logConsumers.add { log.info { "[CONTAINER] ${it.utf8String}" } }
            container.start()
            block(container)
        }
    }
}
