package io.orangebuffalo.testcontainers.playwright.junit

import java.lang.reflect.AnnotatedElement
import kotlin.reflect.KClass

internal fun hasAnnotation(element: AnnotatedElement, annotationClass: KClass<out Annotation>): Boolean {
    return when (element) {
        is Class<*> -> getAnnotation(element, annotationClass) != null
        else -> getAnnotationWithoutInheritance(element, annotationClass) != null
    }
}

internal fun <A : Annotation> getAnnotation(element: Class<*>, annotationClass: KClass<out A>): A? {
    var current: Class<*>? = element
    while (current != null) {
        val direct = getDirectAnnotation(current, annotationClass)
        if (direct != null) return direct
        val meta = findMetaAnnotation(current, annotationClass)
        if (meta != null) return meta

        current.interfaces.forEach { iface ->
            val result = getAnnotation(iface, annotationClass)
            if (result != null) return result
        }

        current = current.superclass
    }
    return null
}

private fun <A : Annotation> getAnnotationWithoutInheritance(element: AnnotatedElement, annotationClass: KClass<out A>): A? {
    val direct = getDirectAnnotation(element, annotationClass)
    if (direct != null) return direct
    return findMetaAnnotation(element, annotationClass)
}

private fun <A : Annotation> getDirectAnnotation(element: AnnotatedElement, annotationClass: KClass<out A>): A? {
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
