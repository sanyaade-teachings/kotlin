package org.jetbrains.kotlin.konan.library

import org.jetbrains.kotlin.konan.properties.propertyList
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.interopFlag
import org.jetbrains.kotlin.library.irProviderName

const val KLIB_PROPERTY_LINKED_OPTS = "linkerOpts"
const val KLIB_PROPERTY_INCLUDED_HEADERS = "includedHeaders"

interface TargetedLibrary {
    val targetList: List<String>
    val includedPaths: List<String>
}

interface BitcodeLibrary : TargetedLibrary {
    val bitcodePaths: List<String>
}

interface KonanLibrary : BitcodeLibrary, KotlinLibrary {
    val linkerOpts: List<String>

    override val hasAbi: Boolean
        get() = super.hasAbi || (interopFlag == "true" && irProviderName == KLIB_INTEROP_IR_PROVIDER_IDENTIFIER)
}

val KonanLibrary.includedHeaders
    get() = manifestProperties.propertyList(KLIB_PROPERTY_INCLUDED_HEADERS, escapeInQuotes = true)
