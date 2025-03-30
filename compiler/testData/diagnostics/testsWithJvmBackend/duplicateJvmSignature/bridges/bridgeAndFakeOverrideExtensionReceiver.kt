// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER
// ISSUE: KT-13712

interface B<T> {
    fun T.foo() {}
}

open class A {
    fun Any.foo() {}
}

<!CONFLICTING_INHERITED_JVM_DECLARATIONS!>class C : B<String>, A() {
    override fun String.foo() {}
}<!>
