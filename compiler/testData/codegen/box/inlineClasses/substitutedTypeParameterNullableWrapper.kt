// TARGET_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

class Wrapper<T>(val value: T)

OPTIONAL_JVM_INLINE_ANNOTATION
value class WithWrapper<T : Any>(val x: Wrapper<T>?)

object Example {
    @JvmStatic
    fun <R : Any> WithWrapper<Int>.genericArgument(x: WithWrapper<R>) {}
    @JvmStatic
    fun WithWrapper<Int>.instantiatedArgument(x: WithWrapper<String>) {}
    @JvmStatic
    fun returnType(x: WithWrapper<String>): WithWrapper<Int> = WithWrapper(Wrapper(1))
    @JvmStatic
    fun nullableReturnType(x: WithWrapper<String>): WithWrapper<Int>? = null
    @JvmStatic
    fun <R : Any> genericReturnType(x: R): WithWrapper<R> = WithWrapper(Wrapper(x))

    val instantiatedProperty: WithWrapper<Int> = WithWrapper(Wrapper(1))
    val nullableProperty: WithWrapper<Int>? = null
}

fun box(): String {
    val withWrapperInt = WithWrapper(Wrapper(1))
    val withWrapperString = WithWrapper(Wrapper("x"))

    with(Example) {
        withWrapperInt.genericArgument(withWrapperString)
        withWrapperInt.instantiatedArgument(withWrapperString)
        val x = returnType(withWrapperString)
        val y = nullableReturnType(withWrapperString)
        val z = genericReturnType(withWrapperInt)
        val a = instantiatedProperty
        val b = nullableProperty
    }

    val inlineClass = WithWrapper::class.java
    val wrapperClass = Wrapper::class.java
    val exampleClass = Example::class.java
    val methods = exampleClass.getDeclaredMethods()

    val genericArgumentMethod = methods.single { it.name.startsWith("genericArgument") }
    require(genericArgumentMethod.toGenericString() == "public static final <R> void ${exampleClass.name}.${genericArgumentMethod.name}(${wrapperClass.name}<java.lang.Integer>,${wrapperClass.name}<R>)") {
        "Unexpected method signature for 'genericArgument': ${genericArgumentMethod.toGenericString()}"
    }

    val instantiatedArgumentMethod = methods.single { it.name.startsWith("instantiatedArgument") }
    require(instantiatedArgumentMethod.toGenericString() == "public static final void ${exampleClass.name}.${instantiatedArgumentMethod.name}(${wrapperClass.name}<java.lang.Integer>,${wrapperClass.name}<java.lang.String>)") {
        "Unexpected method signature for 'instantiatedArgument': ${instantiatedArgumentMethod.toGenericString()}"
    }

    val returnTypeMethod = methods.single { it.name.startsWith("returnType") }
    require(returnTypeMethod.toGenericString() == "public static final ${wrapperClass.name}<java.lang.Integer> ${exampleClass.name}.${returnTypeMethod.name}(${wrapperClass.name}<java.lang.String>)") {
        "Unexpected method signature for 'returnType': ${returnTypeMethod.toGenericString()}"
    }

    val nullableReturnTypeMethod = methods.single { it.name.startsWith("nullableReturnType") }
    require(nullableReturnTypeMethod.toGenericString() == "public static final ${inlineClass.name}<java.lang.Integer> ${exampleClass.name}.${nullableReturnTypeMethod.name}(${wrapperClass.name}<java.lang.String>)") {
        "Unexpected method signature for 'nullableReturnType': ${nullableReturnTypeMethod.toGenericString()}"
    }

    val genericReturnTypeMethod = methods.single { it.name.startsWith("genericReturnType") }
    require(genericReturnTypeMethod.toGenericString() == "public static final <R> ${wrapperClass.name}<R> ${exampleClass.name}.${genericReturnTypeMethod.name}(R)") {
        "Unexpected method signature for 'genericReturnType': ${genericReturnTypeMethod.toGenericString()}"
    }

    val instantiatedPropertyGetter = methods.single { it.name.startsWith("getInstantiatedProperty") }
    require(instantiatedPropertyGetter.toGenericString() == "public final ${wrapperClass.name}<java.lang.Integer> ${exampleClass.name}.${instantiatedPropertyGetter.name}()") {
        "Unexpected method signature for 'getInstantiatedProperty': ${instantiatedPropertyGetter.toGenericString()}"
    }

    val nullablePropertyGetter = methods.single { it.name.startsWith("getNullableProperty") }
    require(nullablePropertyGetter.toGenericString() == "public final ${inlineClass.name}<java.lang.Integer> ${exampleClass.name}.${nullablePropertyGetter.name}()") {
        "Unexpected method signature for 'getNullableProperty': ${nullablePropertyGetter.toGenericString()}"
    }

    return "OK"
}