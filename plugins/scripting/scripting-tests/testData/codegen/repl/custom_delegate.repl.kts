// SNIPPET

import kotlin.reflect.KProperty

class CustomDelegate {
    private var value: String = "OK"

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: String) {
        value = newValue
    }
}

//interface I {
//    val str: String
//}

// SNIPPET

//val u = object : I {
//    override val str by CustomDelegate()
//}

val str by CustomDelegate()

str

// EXPECTED: <res> == "OK"

