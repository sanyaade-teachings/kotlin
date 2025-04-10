/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.EqualsOverrideTrustworthiness
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.computeEqualsOverrideTrustworthiness
import org.jetbrains.kotlin.fir.expressions.ExhaustivenessStatus
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.hasElseBranch
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.types.coneType

object FirWhenEqualsOverrideChecker : FirWhenExpressionChecker(mppKind = MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirWhenExpression) {
        if (expression.hasElseBranch()) return
        val exhaustivenessStatus = expression.exhaustivenessStatus as? ExhaustivenessStatus.ProperlyExhaustive ?: return
        val subjectType = expression.subjectVariable?.returnTypeRef?.coneType ?: return
        val trustworthiness = computeEqualsOverrideTrustworthiness(subjectType, context.session, context.scopeSession)

        if (trustworthiness < EqualsOverrideTrustworthiness.SAFE_FOR_EXHAUSTIVENESS) {
            for (symbolEqualsCheck in exhaustivenessStatus.symbolEqualsChecks) {
                val symbol = symbolEqualsCheck.classId.toSymbol(context.session)

                // If the symbol comes from a dependency, we decide to trust that it's sane.
                // This is to avoid upsetting users who use carefully verified sealed classes
                // from a library.
                if (symbol?.moduleData == context.session.moduleData) {
                    reporter.reportOn(
                        source = symbolEqualsCheck.source ?: expression.subjectVariable?.source,
                        factory = FirErrors.UNSAFE_EXHAUSTIVENESS,
                        symbolEqualsCheck.classId,
                    )
                }
            }
        }
    }
}
