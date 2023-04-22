package io.orangebuffalo.testcontainers.playwright

import io.orangebuffalo.testcontainers.playwright.junit.PlaywrightTestcontainersExtension
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


}
