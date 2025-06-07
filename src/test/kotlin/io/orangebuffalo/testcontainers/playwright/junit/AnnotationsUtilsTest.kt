package io.orangebuffalo.testcontainers.playwright.junit

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AnnotationsUtilsTest {
    @Nested
    @DisplayName("hasAnnotation")
    inner class HasAnnotation {
        @Test
        fun `returns true for directly annotated class`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.DirectlyAnnotated::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns false for non-annotated class`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.NotAnnotated::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            ).shouldBeFalse()
        }

        @Test
        fun `returns true for annotation from superclass`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.SuperAnnotated::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns true for annotation from multiple inheritance levels`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.MultiLevelInheritance::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns true for annotation from interface`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.ImplementsAnnotatedInterface::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns true for annotation from one of multiple interfaces`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.ImplementsMultipleInterfaces::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns true for meta-annotation on class`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.MetaAnnotated::class.java,
                AnnotationsUtilsTestFixtures.MetaAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns true for meta-annotation from superclass`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.InheritsMetaAnnotation::class.java,
                AnnotationsUtilsTestFixtures.MetaAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns true for meta-annotation from interface`() {
            hasAnnotation(
                AnnotationsUtilsTestFixtures.ImplementsMetaAnnotatedInterface::class.java,
                AnnotationsUtilsTestFixtures.MetaAnnotation::class
            ).shouldBeTrue()
        }

        @Test
        fun `returns true for annotation on method`() {
            val method = AnnotationsUtilsTestFixtures.HasAnnotatedMethod::class.java
                .getDeclaredMethod("annotatedMethod")
            hasAnnotation(method, AnnotationsUtilsTestFixtures.TestAnnotation::class).shouldBeTrue()
        }

        @Test
        fun `returns true for annotation on parameter`() {
            val method = AnnotationsUtilsTestFixtures.HasAnnotatedParameter::class.java
                .getDeclaredMethod("methodWithAnnotatedParameter", String::class.java)
            val parameter = method.parameters[0]
            hasAnnotation(parameter, AnnotationsUtilsTestFixtures.TestAnnotation::class).shouldBeTrue()
        }
    }

    @Nested
    @DisplayName("getAnnotation")
    inner class GetAnnotation {
        @Test
        fun `returns direct annotation instance`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.DirectlyAnnotated::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class,
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.TestAnnotation::class)
        }

        @Test
        fun `returns meta-annotation instance`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.MetaAnnotated::class.java,
                AnnotationsUtilsTestFixtures.MetaAnnotation::class,
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.MetaAnnotation::class)
        }

        @Test
        fun `returns null if annotation is missing`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.NotAnnotated::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class,
            ).shouldBeNull()
        }

        @Test
        fun `returns annotation from superclass`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.SuperAnnotated::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.TestAnnotation::class)
        }

        @Test
        fun `returns annotation from multiple inheritance levels`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.MultiLevelInheritance::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.TestAnnotation::class)
        }

        @Test
        fun `returns annotation from interface`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.ImplementsAnnotatedInterface::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.TestAnnotation::class)
        }

        @Test
        fun `returns annotation from one of multiple interfaces`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.ImplementsMultipleInterfaces::class.java,
                AnnotationsUtilsTestFixtures.TestAnnotation::class
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.TestAnnotation::class)
        }

        @Test
        fun `returns meta-annotation from superclass`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.InheritsMetaAnnotation::class.java,
                AnnotationsUtilsTestFixtures.MetaAnnotation::class
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.MetaAnnotation::class)
        }

        @Test
        fun `returns meta-annotation from interface`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.ImplementsMetaAnnotatedInterface::class.java,
                AnnotationsUtilsTestFixtures.MetaAnnotation::class
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.MetaAnnotation::class)
        }

        @Test
        fun `returns meta-annotation from multiple inheritance levels`() {
            getAnnotation(
                AnnotationsUtilsTestFixtures.MultiLevelMetaInheritance::class.java,
                AnnotationsUtilsTestFixtures.MetaAnnotation::class
            )?.annotationClass.shouldBe(AnnotationsUtilsTestFixtures.MetaAnnotation::class)
        }
    }
}

object AnnotationsUtilsTestFixtures {
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TestAnnotation

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MetaAnnotation

    @MetaAnnotation
    annotation class AnnotatedWithMetaAnnotation

    @TestAnnotation
    open class BaseWithAnnotation

    @AnnotatedWithMetaAnnotation
    open class BaseWithMetaAnnotation

    open class MiddleClass : BaseWithAnnotation()

    @TestAnnotation
    interface AnnotatedInterface

    @AnnotatedWithMetaAnnotation
    interface MetaAnnotatedInterface

    interface NonAnnotatedInterface

    @TestAnnotation
    class DirectlyAnnotated

    @AnnotatedWithMetaAnnotation
    class MetaAnnotated

    class SuperAnnotated : BaseWithAnnotation()

    class MultiLevelInheritance : MiddleClass()

    open class InheritsMetaAnnotation : BaseWithMetaAnnotation()

    class MultiLevelMetaInheritance : InheritsMetaAnnotation()

    class ImplementsAnnotatedInterface : AnnotatedInterface

    class ImplementsMetaAnnotatedInterface : MetaAnnotatedInterface

    class ImplementsMultipleInterfaces : NonAnnotatedInterface, AnnotatedInterface

    class NotAnnotated

    class HasAnnotatedMethod {
        @Suppress("unused")
        @TestAnnotation
        fun annotatedMethod() {
        }
    }

    class HasAnnotatedParameter {
        @Suppress("unused", "UNUSED_PARAMETER")
        fun methodWithAnnotatedParameter(@TestAnnotation param: String) {
        }
    }
}
