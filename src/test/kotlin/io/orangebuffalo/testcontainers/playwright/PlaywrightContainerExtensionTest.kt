package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.TimeoutError
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.orangebuffalo.testcontainers.playwright.junit.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@DisplayName("JUnit extension")
internal class PlaywrightContainerExtensionTest {

    @Nested
    @DisplayName("should provide default container without PlaywrightTestcontainersConfig")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class DefaultContainer {

        @Test
        fun `that runs`(playwright: PlaywrightContainerApi) {
            val page = playwright.chromium().newPage()
            page.navigate("https://github.com/microsoft/playwright")
            page.title().shouldContain("Playwright")
        }
    }

    @Nested
    @DisplayName("should provide low level API")
    @UseExtensionUnderTest
    inner class LowLevelApi {

        @Test
        fun `that opens chromium browser`(playwright: PlaywrightContainerApi) {
            val page = playwright.chromium().openAndVerifyTestPage()
            page.shouldRunInChromium()
        }

        @Test
        fun `that opens firefox browser`(playwright: PlaywrightContainerApi) {
            val page = playwright.firefox().openAndVerifyTestPage()
            page.shouldRunInFirefox()
        }

        @Test
        fun `that opens webkit browser`(playwright: PlaywrightContainerApi) {
            val page = playwright.webkit().openAndVerifyTestPage()
            page.shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should support parallel execution")
    @UseExtensionUnderTest
    inner class ParallelExecution {

        @Test
        @Execution(ExecutionMode.CONCURRENT)
        fun `branch 1`(playwright: PlaywrightContainerApi) {
            playwright.chromium().openAndVerifyTestPage()
        }

        @Test
        @Execution(ExecutionMode.CONCURRENT)
        fun `branch 2`(playwright: PlaywrightContainerApi) {
            playwright.chromium().openAndVerifyTestPage()
        }

        @Test
        @Execution(ExecutionMode.CONCURRENT)
        fun `branch 3`(playwright: PlaywrightContainerApi) {
            playwright.chromium().openAndVerifyTestPage()
        }
    }

    @Nested
    @DisplayName("should provide chromium as default")
    @UseExtensionUnderTest
    inner class DefaultInjections {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }

        @Test
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by parameter annotation")
    @UseExtensionUnderTest
    inner class ChromiumInjectsWithParameterAnnotations {

        @Test
        fun `for browser context`(@RequiresChromium browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }

        @Test
        fun `for page`(@RequiresChromium page: Page) {
            page.navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by parameter meta annotation")
    @UseExtensionUnderTest
    inner class ChromiumInjectsWithParameterMetaAnnotations {

        @Test
        fun `for browser context`(@MetaRequiresChromium browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }

        @Test
        fun `for page`(@MetaRequiresChromium page: Page) {
            page.navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by method annotation")
    @UseExtensionUnderTest
    inner class ChromiumInjectsWithMethodAnnotations {

        @Test
        @RequiresChromium
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }

        @Test
        @RequiresChromium
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by method meta annotation")
    @UseExtensionUnderTest
    inner class ChromiumInjectsWithMethodMetaAnnotations {

        @Test
        @MetaRequiresChromium
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }

        @Test
        @MetaRequiresChromium
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by class annotation")
    @UseExtensionUnderTest
    @RequiresChromium
    inner class ChromiumInjectsWithClassAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }

        @Test
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by class meta annotation")
    @UseExtensionUnderTest
    @MetaRequiresChromium
    inner class ChromiumInjectsWithClassMetaAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }

        @Test
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide firefox by parameter annotation")
    @UseExtensionUnderTest
    inner class FirefoxInjectsWithParameterAnnotations {

        @Test
        fun `for browser context`(@RequiresFirefox browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }

        @Test
        fun `for page`(@RequiresFirefox page: Page) {
            page.navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by parameter meta annotation")
    @UseExtensionUnderTest
    inner class FirefoxInjectsWithParameterMetaAnnotations {

        @Test
        fun `for browser context`(@MetaRequiresFirefox browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }

        @Test
        fun `for page`(@MetaRequiresFirefox page: Page) {
            page.navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by method annotation")
    @UseExtensionUnderTest
    inner class FirefoxInjectsWithMethodAnnotations {

        @Test
        @RequiresFirefox
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }

        @Test
        @RequiresFirefox
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by method meta annotation")
    @UseExtensionUnderTest
    inner class FirefoxInjectsWithMethodMetaAnnotations {

        @Test
        @MetaRequiresFirefox
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }

        @Test
        @MetaRequiresFirefox
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by class annotation")
    @UseExtensionUnderTest
    @RequiresFirefox
    inner class FirefoxInjectsWithClassAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }

        @Test
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by class meta annotation")
    @UseExtensionUnderTest
    @MetaRequiresFirefox
    inner class FirefoxInjectsWithClassMetaAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }

        @Test
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide webkit by parameter annotation")
    @UseExtensionUnderTest
    inner class WebkitInjectsWithParameterAnnotations {

        @Test
        fun `for browser context`(@RequiresWebkit browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }

        @Test
        fun `for page`(@RequiresWebkit page: Page) {
            page.navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by parameter meta annotation")
    @UseExtensionUnderTest
    inner class WebkitInjectsWithParameterMetaAnnotations {

        @Test
        fun `for browser context`(@MetaRequiresWebkit browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }

        @Test
        fun `for page`(@MetaRequiresWebkit page: Page) {
            page.navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by method annotation")
    @UseExtensionUnderTest
    inner class WebkitInjectsWithMethodAnnotations {

        @Test
        @RequiresWebkit
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }

        @Test
        @RequiresWebkit
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by method meta annotation")
    @UseExtensionUnderTest
    inner class WebkitInjectsWithMethodMetaAnnotations {

        @Test
        @MetaRequiresWebkit
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }

        @Test
        @MetaRequiresWebkit
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by class annotation")
    @UseExtensionUnderTest
    @RequiresWebkit
    inner class WebkitInjectsWithClassAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }

        @Test
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by class meta annotation")
    @UseExtensionUnderTest
    @MetaRequiresWebkit
    inner class WebkitInjectsWithClassMetaAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }

        @Test
        fun `for page`(page: Page) {
            page.navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should allow to provide browser context options")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @PlaywrightTestcontainersConfig(BrowserContextOptionsConfigurer::class)
    inner class BrowserContextOptions {

        @Test
        fun `for browser context`(browserContext: BrowserContext) = executeTest(browserContext.newPage())

        @Test
        fun `for page`(page: Page) = executeTest(page)

        private fun executeTest(page: Page) {
            val browserScreenWidth = page
                .navigateAndVerify()
                .evaluate("window.screen.width") as Int
            browserScreenWidth.shouldBe(2000)
        }
    }

    class BrowserContextOptionsConfigurer : TestExtensionsConfigurer() {
        override fun createBrowserContextOptions(): Browser.NewContextOptions =
            Browser.NewContextOptions().setViewportSize(2000, 1000)
    }

    @Nested
    @DisplayName("should allow to configure created browser contexts")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @PlaywrightTestcontainersConfig(BrowserContextSetupConfigurer::class)
    inner class BrowserContextSetup {

        @Test
        fun `for browser context`(browserContext: BrowserContext) = executeTest(browserContext.newPage())

        @Test
        fun `for page`(page: Page) = executeTest(page)

        private fun executeTest(page: Page) {
            page.navigateAndVerify()
            val exception = shouldThrow<TimeoutError> {
                page.getByLabel("Non existent").blur()
            }
            exception.message.shouldContain("Timeout 20ms exceeded")
        }
    }

    class BrowserContextSetupConfigurer : TestExtensionsConfigurer() {
        override fun configureBrowserContext(context: BrowserContext) {
            context.setDefaultTimeout(20.0)
        }
    }

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @RequiresChromium
    annotation class MetaRequiresChromium

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @RequiresFirefox
    annotation class MetaRequiresFirefox

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @RequiresWebkit
    annotation class MetaRequiresWebkit

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @PlaywrightTestcontainersConfig(TestExtensionsConfigurer::class)
    annotation class UseExtensionUnderTest

    open class TestExtensionsConfigurer : PlaywrightTestcontainersConfigurer {
        override fun setupContainer(container: PlaywrightContainer) {
            container.connectToWebServer()
        }
    }
}
