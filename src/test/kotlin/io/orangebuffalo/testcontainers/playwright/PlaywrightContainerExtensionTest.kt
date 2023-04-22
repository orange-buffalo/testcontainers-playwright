package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.BrowserContext
import io.orangebuffalo.testcontainers.playwright.junit.PlaywrightTestcontainersExtension
import io.orangebuffalo.testcontainers.playwright.junit.RequiresChromium
import io.orangebuffalo.testcontainers.playwright.junit.RequiresFirefox
import io.orangebuffalo.testcontainers.playwright.junit.RequiresWebkit
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@DisplayName("JUnit extension")
internal class PlaywrightContainerExtensionTest {

    @Nested
    @DisplayName("should provide low level API")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
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
    @ExtendWith(PlaywrightTestcontainersExtension::class)
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
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class DefaultInjections {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by parameter annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class ChromiumInjectsWithParameterAnnotations {

        @Test
        fun `for browser context`(@RequiresChromium browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by parameter meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class ChromiumInjectsWithParameterMetaAnnotations {

        @Test
        fun `for browser context`(@MetaRequiresChromium browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by method annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class ChromiumInjectsWithMethodAnnotations {

        @Test
        @RequiresChromium
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by method meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class ChromiumInjectsWithMethodMetaAnnotations {

        @Test
        @MetaRequiresChromium
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by class annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @RequiresChromium
    inner class ChromiumInjectsWithClassAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide chromium by class meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @MetaRequiresChromium
    inner class ChromiumInjectsWithClassMetaAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInChromium()
        }
    }

    @Nested
    @DisplayName("should provide firefox by parameter annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class FirefoxInjectsWithParameterAnnotations {

        @Test
        fun `for browser context`(@RequiresFirefox browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by parameter meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class FirefoxInjectsWithParameterMetaAnnotations {

        @Test
        fun `for browser context`(@MetaRequiresFirefox browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by method annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class FirefoxInjectsWithMethodAnnotations {

        @Test
        @RequiresFirefox
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by method meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class FirefoxInjectsWithMethodMetaAnnotations {

        @Test
        @MetaRequiresFirefox
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by class annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @RequiresFirefox
    inner class FirefoxInjectsWithClassAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide firefox by class meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @MetaRequiresFirefox
    inner class FirefoxInjectsWithClassMetaAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInFirefox()
        }
    }

    @Nested
    @DisplayName("should provide webkit by parameter annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class WebkitInjectsWithParameterAnnotations {

        @Test
        fun `for browser context`(@RequiresWebkit browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by parameter meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class WebkitInjectsWithParameterMetaAnnotations {

        @Test
        fun `for browser context`(@MetaRequiresWebkit browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by method annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class WebkitInjectsWithMethodAnnotations {

        @Test
        @RequiresWebkit
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by method meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    inner class WebkitInjectsWithMethodMetaAnnotations {

        @Test
        @MetaRequiresWebkit
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by class annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @RequiresWebkit
    inner class WebkitInjectsWithClassAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
        }
    }

    @Nested
    @DisplayName("should provide webkit by class meta annotation")
    @ExtendWith(PlaywrightTestcontainersExtension::class)
    @MetaRequiresWebkit
    inner class WebkitInjectsWithClassMetaAnnotations {

        @Test
        fun `for browser context`(browserContext: BrowserContext) {
            browserContext.newPage()
                .navigateAndVerify()
                .shouldRunInWebkit()
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
}
