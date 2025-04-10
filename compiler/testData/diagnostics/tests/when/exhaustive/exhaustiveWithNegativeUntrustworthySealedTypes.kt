// RUN_PIPELINE_TILL: BACKEND

open class PhantomEquivalence {
    override fun equals(other: Any?) = other is PhantomEquivalence
}

sealed interface Variants {
    data object A : Variants
    object B : PhantomEquivalence(), Variants
}

fun foo(v: Variants): String {
    if (v == Variants.A) {
        return "A"
    }

    return <!NO_ELSE_IN_WHEN!>when<!> (v) {
        Variants.B -> "B"
    }
}

fun bar(v: Variants): String {
    if (v == Variants.A) {
        return "A"
    }

    return when (v) {
        Variants.B -> "B"
        else -> "C"
    }
}
