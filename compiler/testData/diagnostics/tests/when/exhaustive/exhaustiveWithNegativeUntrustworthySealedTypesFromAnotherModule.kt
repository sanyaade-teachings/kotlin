// FIR_IDENTICAL
// RUN_PIPELINE_TILL: FRONTEND
// MODULE: a

open class PhantomEquivalence {
    override fun equals(other: Any?) = other is PhantomEquivalence
}

sealed interface Variants {
    data object A : Variants
    object B : PhantomEquivalence(), Variants
}

// MODULE: b(a)

fun foo(v: Variants): String {
    if (v == Variants.A) {
        return "A"
    }

    return <!NO_ELSE_IN_WHEN!>when<!> (v) {
        Variants.B -> "B"
    }
}
