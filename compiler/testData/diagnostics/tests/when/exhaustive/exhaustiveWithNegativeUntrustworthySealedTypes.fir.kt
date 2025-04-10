// RUN_PIPELINE_TILL: BACKEND
// RENDER_DIAGNOSTICS_MESSAGES

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

    return when (<!UNSAFE_EXHAUSTIVENESS("Variants.A")!>v<!>) {
        <!UNSAFE_EXHAUSTIVENESS("Variants.B")!>Variants.B<!> -> "B"
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
