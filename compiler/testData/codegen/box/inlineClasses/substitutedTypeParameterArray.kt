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
    val fields = exampleClass.getDeclaredFields()

    val genericArgumentMethod = methods.single { it.name.startsWith("genericArgument") }
    require(genericArgumentMethod.toGenericString() == "public static final <R> void ${exampleClass.name}.${genericArgumentMethod.name}(java.lang.Object[],java.lang.Object[])") {
        "Unexpected method signature for 'genericArgument': ${genericArgumentMethod.toGenericString()}"
    }

    val instantiatedArgumentMethod = methods.single { it.name.startsWith("instantiatedArgument") }
    require(instantiatedArgumentMethod.toGenericString() == "public static final void ${exampleClass.name}.${instantiatedArgumentMethod.name}(java.lang.Object[],java.lang.Object[])") {
        "Unexpected method signature for 'instantiatedArgument': ${instantiatedArgumentMethod.toGenericString()}"
    }

    val returnTypeMethod = methods.single { it.name.startsWith("returnType") }
    require(returnTypeMethod.toGenericString() == "public static final java.lang.Object[] ${exampleClass.name}.${returnTypeMethod.name}(java.lang.Object[])") {
        "Unexpected method signature for 'returnType': ${returnTypeMethod.toGenericString()}"
    }

    val nullableReturnTypeMethod = methods.single { it.name.startsWith("nullableReturnType") }
    require(nullableReturnTypeMethod.toGenericString() == "public static final java.lang.Object[] ${exampleClass.name}.${nullableReturnTypeMethod.name}(java.lang.Object[])") {
        "Unexpected method signature for 'nullableReturnType': ${nullableReturnTypeMethod.toGenericString()}"
    }

    val genericReturnTypeMethod = methods.single { it.name.startsWith("genericReturnType") }
    require(genericReturnTypeMethod.toGenericString() == "public static final <R> java.lang.Object[] ${exampleClass.name}.${genericReturnTypeMethod.name}(R)") {
        "Unexpected method signature for 'genericReturnType': ${genericReturnTypeMethod.toGenericString()}"
    }

    val instantiatedPropertyGetter = methods.single { it.name.startsWith("getInstantiatedProperty") }
    require(instantiatedPropertyGetter.toGenericString() == "public final java.lang.Object[] ${exampleClass.name}.${instantiatedPropertyGetter.name}()") {
        "Unexpected method signature for 'getInstantiatedProperty': ${instantiatedPropertyGetter.toGenericString()}"
    }

    val nullablePropertyGetter = methods.single { it.name.startsWith("getNullableProperty") }
    require(nullablePropertyGetter.toGenericString() == "public final java.lang.Object[] ${exampleClass.name}.${nullablePropertyGetter.name}()") {
        "Unexpected method signature for 'getNullableProperty': ${nullablePropertyGetter.toGenericString()}"
    }

    val instantiatedPropertyField = fields.single { it.name.startsWith("instantiatedProperty") }
    require(instantiatedPropertyField.toGenericString() == "private static final java.lang.Object[] ${exampleClass.name}.${instantiatedPropertyField.name}") {
        "Unexpected field signature for 'instantiatedProperty': ${instantiatedPropertyField.toGenericString()}"
    }

    val nullablePropertyField = fields.single { it.name.startsWith("nullableProperty") }
    require(nullablePropertyField.toGenericString() == "private static final java.lang.Object[] ${exampleClass.name}.${nullablePropertyField.name}") {
        "Unexpected field signature for 'nullableProperty': ${nullablePropertyField.toGenericString()}"
    }

    return "OK"
}