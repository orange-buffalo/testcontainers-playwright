package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain

internal fun Browser.openAndVerifyTestPage(): Page {
    val page = newPage()
    page.navigate("https://github.com/microsoft/playwright")
    page.title().shouldContain("Playwright")
    return page
}

internal fun Page.shouldRunInChromium() {
    val isChromium = evaluate("navigator.userAgent.indexOf('Chrome') !== -1") as Boolean
    isChromium.shouldBeTrue()
}

internal fun Page.shouldRunInFirefox() {
    val isFirefox = evaluate("navigator.userAgent.indexOf('Firefox') !== -1") as Boolean
    isFirefox.shouldBeTrue()
}

internal fun Page.shouldRunInWebkit() {
    val isWebKit = evaluate("navigator.userAgent.indexOf('WebKit') !== -1") as Boolean
    isWebKit.shouldBeTrue()
}
