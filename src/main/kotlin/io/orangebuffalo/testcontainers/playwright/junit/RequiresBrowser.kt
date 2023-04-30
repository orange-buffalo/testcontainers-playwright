package io.orangebuffalo.testcontainers.playwright.junit

/**
 * Instructs [PlaywrightExtension] to inject Playwright API instance that are bound to Chromium.
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresChromium

/**
 * Instructs [PlaywrightExtension] to inject Playwright API instance that are bound to Firefox.
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresFirefox

/**
 * Instructs [PlaywrightExtension] to inject Playwright API instance that are bound to WebKit.
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresWebkit
