/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.classpathDiff.impl

import org.jetbrains.kotlin.incremental.impl.ExtraClassInfoGenerator

//TODO(cleanup) maybe rename file or w/e
internal interface ClassMultiHashProvider {
    /**
     * Provides multi-hash of required class' abis.
     *
     * Some of these classes might reference other classes, so the implementation is required to
     * fetch transitive dependencies, deduplicate the whole dependency tree, and only then
     * to apply the multihash (if it's symmetric, which is usually a good trait)
     */
    fun searchAndGetFullAbiHashOfUsedClasses(inlinedClassPrefix: String): Long
}

internal class ExtraInfoGeneratorWithInlinedClassSnapshotting(
    private val classMultiHashProvider: ClassMultiHashProvider
) : ExtraClassInfoGenerator() {
    override fun calculateInlineMethodHash(inlinedClassPrefix: String, ownMethodHash: Long): Long {
        return ownMethodHash xor classMultiHashProvider.searchAndGetFullAbiHashOfUsedClasses(inlinedClassPrefix)
    }
}
