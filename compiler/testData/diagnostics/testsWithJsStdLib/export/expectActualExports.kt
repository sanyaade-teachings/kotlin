// FIR_IDENTICAL
// OPT_IN: kotlin.js.ExperimentalJsExport
// LANGUAGE: +MultiPlatformProjects
// RENDER_DIAGNOSTICS_MESSAGES

// MODULE: commonMain
// FILE: Common.kt
package sample

// Functions
@JsExport expect fun foo(): Int

@JsExport expect fun bar(): Int

// Classes
@JsExport expect class Foo
@JsExport expect class Bar
@JsExport expect class Baz {
    <!WRONG_EXPORTED_DECLARATION("suspend function"), WRONG_EXPORTED_DECLARATION{METADATA}("suspend function")!>suspend fun foo(): Int<!>
}
@JsExport expect class Nested {
    interface A
}
@JsExport expect class Test1 {
    fun test()
}
@JsExport expect class Test2 {
    fun test()
}
@JsExport expect class Test3 {
    fun test()
}

// MODULE: jsMain()()(commonMain)
// FILE: js.kt
package sample

// Functions
<!NOT_EXPORTED_ACTUAL_DECLARATION_WHILE_EXPECT_IS_EXPORTED!>actual fun foo()<!> = 42

@JsExport actual fun bar() = 42

// Classes
actual class <!NOT_EXPORTED_ACTUAL_DECLARATION_WHILE_EXPECT_IS_EXPORTED!>Foo<!>

@JsExport actual class Bar {
    <!WRONG_EXPORTED_DECLARATION("suspend function")!>suspend fun foo()<!> = 42
}

@JsExport actual class Nested {
    @JsExport.Ignore actual interface <!NOT_EXPORTED_ACTUAL_DECLARATION_WHILE_EXPECT_IS_EXPORTED!>A<!>
}

@JsExport class ExportedOne { fun test() {} }
class NotExportedOne { fun test() {} }
@JsExport interface ExportedInterface { fun test() }

<!NOT_EXPORTED_ACTUAL_DECLARATION_WHILE_EXPECT_IS_EXPORTED!>actual typealias Test1 = NotExportedOne<!>
actual typealias Test2 = ExportedOne
actual typealias <!ACTUAL_WITHOUT_EXPECT("actual typealias Test3 = ExportedInterface; The following declaration is incompatible because class kinds are different (class, interface, object, enum, annotation):    expect class Test3 : Any")!>Test3<!> = ExportedInterface
