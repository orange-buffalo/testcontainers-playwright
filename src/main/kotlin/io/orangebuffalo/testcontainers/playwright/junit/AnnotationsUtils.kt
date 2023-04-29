package io.orangebuffalo.testcontainers.playwright.junit

import java.lang.reflect.AnnotatedElement
import kotlin.reflect.KClass

internal fun hasAnnotation(element: AnnotatedElement, annotationClass: KClass<out Annotation>): Boolean {
    return findAnnotation(element, annotationClass) != null
            || findMetaAnnotation(element, annotationClass) != null
}

internal fun <A : Annotation> getAnnotation(element: AnnotatedElement, annotationClass: KClass<out A>): A? {
    return findAnnotation(element, annotationClass)
        ?: findMetaAnnotation(element, annotationClass)
}

private fun <A : Annotation> findAnnotation(element: AnnotatedElement, annotationClass: KClass<out A>): A? {
    return element.getAnnotation(annotationClass.java)
}

@Suppress("UNCHECKED_CAST")
private fun <A : Annotation> findMetaAnnotation(
    element: AnnotatedElement,
    annotationClass: KClass<out A>,
    visited: MutableSet<AnnotatedElement> = mutableSetOf()
): A? {
    val annotations = element.annotations
    for (annotation in annotations) {
        if (annotation.annotationClass == annotationClass) {
            return annotation as A
        }
        val next = annotation.annotationClass.java
        if (visited.add(next)) {
            val nextResult = findMetaAnnotation(next, annotationClass, visited)
            if (nextResult != null) {
                return nextResult
            }
        }
    }
    return null
}
