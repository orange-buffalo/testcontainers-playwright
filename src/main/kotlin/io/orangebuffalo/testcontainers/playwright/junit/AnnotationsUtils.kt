package io.orangebuffalo.testcontainers.playwright.junit

import java.lang.reflect.AnnotatedElement
import kotlin.reflect.KClass

internal fun hasAnnotation(element: AnnotatedElement, annotationClass: KClass<out Annotation>): Boolean {
    return isAnnotatedWith(element, annotationClass) || isMetaAnnotatedWith(element, annotationClass)
}

internal fun isAnnotatedWith(element: AnnotatedElement, annotationClass: KClass<out Annotation>): Boolean {
    return element.isAnnotationPresent(annotationClass.java)
}

internal fun isMetaAnnotatedWith(
    element: AnnotatedElement,
    annotationClass: KClass<out Annotation>,
    visited: MutableSet<AnnotatedElement> = mutableSetOf()
): Boolean {
    val annotations = element.annotations
    for (annotation in annotations) {
        if (annotation.annotationClass == annotationClass) {
            return true
        }
        val next = annotation.annotationClass.java
        if (visited.add(next) && isMetaAnnotatedWith(next, annotationClass, visited)) {
            return true
        }
    }
    return false
}
