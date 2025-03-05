// LANGUAGE: +MultiPlatformProjects

// MODULE: lib-common
expect fun foo()

// MODULE: lib-platform()()(lib-common)
actual fun foo() {}
fun bar() {}

// MODULE: app-common(lib-common)
fun test_common() {
    foo()
}

// MODULE: app-platform(lib-platform)()(app-common)
fun test_platform() {
    foo()
    bar()
}

fun box() = "OK"
