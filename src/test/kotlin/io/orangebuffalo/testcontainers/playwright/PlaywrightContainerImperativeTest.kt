package io.orangebuffalo.testcontainers.playwright

import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

internal class PlaywrightContainerImperativeTest {

    @Test
    fun shouldProvideValidBrowser() {
        PlaywrightContainer().use { container ->
            container.start()
            val browser = container.registerNewBrowser()
            val page = browser.newPage()
            page.navigate("https://github.com/microsoft/playwright")
            page.title().shouldContain("Playwright")
        }
    }
}
