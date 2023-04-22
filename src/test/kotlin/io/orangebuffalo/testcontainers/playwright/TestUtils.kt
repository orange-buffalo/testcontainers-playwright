package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldContain

internal fun Page.navigateAndVerify(): Page {
    navigate("https://github.com/microsoft/playwright")
    title().shouldContain("Playwright")
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
