// FILE: test.kt

annotation class Anno

class C
    @Anno
    constructor()
{
    @Anno
    constructor(p: String = "") : this()
}

fun box() {
    C()

    C("")
}

// EXPECTATIONS JVM_IR
// test.kt:14 box
// EXPECTATIONS JVM_IR ClassicFrontend
// test.kt:5 <init>
// EXPECTATIONS JVM_IR FIR
// test.kt:6 <init>
// EXPECTATIONS JVM_IR
// test.kt:7 <init>
// test.kt:14 box
// test.kt:16 box
// test.kt:10 <init>
// EXPECTATIONS JVM_IR ClassicFrontend
// test.kt:5 <init>
// EXPECTATIONS JVM_IR FIR
// test.kt:6 <init>
// EXPECTATIONS JVM_IR
// test.kt:7 <init>
// test.kt:10 <init>
// test.kt:16 box
// test.kt:17 box

// EXPECTATIONS JS_IR
// test.kt:14 box
// test.kt:7 <init>
// test.kt:16 box
// test.kt:10 C_init_$Init$
// test.kt:10 C_init_$Init$
// test.kt:7 <init>
// test.kt:17 box
