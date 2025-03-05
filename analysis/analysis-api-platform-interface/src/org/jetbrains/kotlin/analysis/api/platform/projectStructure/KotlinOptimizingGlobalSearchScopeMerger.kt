/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.platform.projectStructure

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi

public class KotlinOptimizingGlobalSearchScopeMerger(private val project: Project) : KotlinGlobalSearchScopeMerger {
    @OptIn(KaExperimentalApi::class)
    private fun <T : Any> Collection<GlobalSearchScope>.applyStrategy(strategy: KotlinGlobalSearchScopeMergeStrategy<T>): Collection<GlobalSearchScope> {
        val applicableScopes = this.filterIsInstance(strategy.targetType.java).ifEmpty { return this@applyStrategy }

        @Suppress("UNCHECKED_CAST")
        val restScopes = (this - applicableScopes) as List<GlobalSearchScope>
        return strategy.uniteScopes(applicableScopes) + restScopes
    }

    @OptIn(KaExperimentalApi::class)
    override fun union(scopes: Collection<GlobalSearchScope>): GlobalSearchScope {
        when {
            scopes.isEmpty() -> return GlobalSearchScope.EMPTY_SCOPE
        }

        val providedStrategies =
            KotlinGlobalSearchScopeMergeStrategy.getMergeStrategies(project)

        val resultingScopes = providedStrategies.fold(scopes) { scopes, strategy ->
            scopes.applyStrategy(strategy)
        }

        return GlobalSearchScope.union(resultingScopes)
    }
}