// WITH_STDLIB
// IGNORE_BACKEND_K1: JVM_IR
// Check that methods are generated only in interface-disabled, class-noCompatibility case

// MODULE: libdisable
// JVM_DEFAULT_MODE: disable
// FILE: libdisable.kt

interface A {
    fun f(t: String): String = t
}


// MODULE: main(libdisable)
// JVM_DEFAULT_MODE: enable
// FILE: main.kt

@JvmDefaultWithoutCompatibility
class Test : A
