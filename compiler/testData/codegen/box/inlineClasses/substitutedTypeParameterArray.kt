// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
value class WithArray<T : Any>(val x: Array<T>)

object Example {
    @JvmStatic
    fun <R : Any> WithArray<Int>.genericArgument(x: WithArray<R>) {}
    @JvmStatic
    fun WithArray<Int>.instantiatedArgument(x: WithArray<String>) {}
    @JvmStatic
    fun returnType(x: WithArray<String>): WithArray<Int> = WithArray(arrayOf(1))
    @JvmStatic
    fun nullableReturnType(x: WithArray<String>): WithArray<Int>? = null
    @JvmStatic
    inline fun <reified R : Any> genericReturnType(x: R): WithArray<R> = WithArray(arrayOf(x))

    val instantiatedProperty: WithArray<Int> = WithArray(arrayOf(1))
    val nullableProperty: WithArray<Int>? = null
}

fun box(): String {
    val withArrayInt = WithArray(arrayOf(1))
    val withArrayString = WithArray(arrayOf("x"))

    with(Example) {
        withArrayInt.genericArgument(withArrayString)
        withArrayInt.instantiatedArgument(withArrayString)
        val x = returnType(withArrayString)
        val y = nullableReturnType(withArrayString)
        val z = genericReturnType(withArrayInt)
        val a = instantiatedProperty
        val b = nullableProperty
    }

    val exampleClass = Example::class.java
    val methods = exampleClass.getDeclaredMethods()

    val genericArgumentMethod = methods.single { it.name.startsWith("genericArgument") }
    require(genericArgumentMethod.toString() == "public static final void ${exampleClass.name}.${genericArgumentMethod.name}(java.lang.Object[],java.lang.Object[])") {
        "Unexpected method signature for 'genericArgument': ${genericArgumentMethod.toString()}"
    }

    val instantiatedArgumentMethod = methods.single { it.name.startsWith("instantiatedArgument") }
    require(instantiatedArgumentMethod.toString() == "public static final void ${exampleClass.name}.${instantiatedArgumentMethod.name}(java.lang.Object[],java.lang.Object[])") {
        "Unexpected method signature for 'instantiatedArgument': ${instantiatedArgumentMethod.toString()}"
    }

    val returnTypeMethod = methods.single { it.name.startsWith("returnType") }
    require(returnTypeMethod.toString() == "public static final java.lang.Object[] ${exampleClass.name}.${returnTypeMethod.name}(java.lang.Object[])") {
        "Unexpected method signature for 'returnType': ${returnTypeMethod.toString()}"
    }

    val nullableReturnTypeMethod = methods.single { it.name.startsWith("nullableReturnType") }
    require(nullableReturnTypeMethod.toString() == "public static final java.lang.Object[] ${exampleClass.name}.${nullableReturnTypeMethod.name}(java.lang.Object[])") {
        "Unexpected method signature for 'nullableReturnType': ${nullableReturnTypeMethod.toString()}"
    }

    val genericReturnTypeMethod = methods.single { it.name.startsWith("genericReturnType") }
    require(genericReturnTypeMethod.toString() == "public static final java.lang.Object[] ${exampleClass.name}.${genericReturnTypeMethod.name}(java.lang.Object)") {
        "Unexpected method signature for 'genericReturnType': ${genericReturnTypeMethod.toString()}"
    }

    val instantiatedPropertyGetter = methods.single { it.name.startsWith("getInstantiatedProperty") }
    require(instantiatedPropertyGetter.toString() == "public final java.lang.Object[] ${exampleClass.name}.${instantiatedPropertyGetter.name}()") {
        "Unexpected method signature for 'getInstantiatedProperty': ${instantiatedPropertyGetter.toString()}"
    }

    val nullablePropertyGetter = methods.single { it.name.startsWith("getNullableProperty") }
    require(nullablePropertyGetter.toString() == "public final java.lang.Object[] ${exampleClass.name}.${nullablePropertyGetter.name}()") {
        "Unexpected method signature for 'getNullableProperty': ${nullablePropertyGetter.toString()}"
    }

    return "OK"
}