/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.plugin

import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.ReturnValueCheckerMode
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationResolvePhase
import org.jetbrains.kotlin.fir.expressions.FirEmptyArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCall
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.StandardClassIds

internal class FirMustUseValuePlacementTransformer private constructor(val session: FirSession) : FirVisitorVoid() {
    override fun visitElement(element: FirElement) {
        // No-op for all children of FirFile/FirClass except other classes
    }

    override fun visitFile(file: FirFile) {
        file.replaceAnnotations(file.symbol.addMustUseValueAnnotation(file.annotations))
        file.acceptChildren(this)
    }

    override fun visitSimpleFunction(simpleFunction: FirSimpleFunction) {
        simpleFunction.replaceAnnotations(simpleFunction.symbol.addMustUseValueAnnotation(simpleFunction.annotations))
    }

    // For future: think about type aliases
    override fun visitRegularClass(regularClass: FirRegularClass) {
        regularClass.replaceAnnotations(regularClass.symbol.addMustUseValueAnnotation(regularClass.annotations))
        // We do not have to place annotations on functions again, if they are not top-level
        regularClass.declarations.filterIsInstance<FirRegularClass>().forEach { it.accept(this) }
    }

    val mustUseReturnValueClassId = StandardClassIds.Annotations.MustUseReturnValue
    val mustUseValueClass by lazy { session.symbolProvider.getClassLikeSymbolByClassId(mustUseReturnValueClassId) as? FirRegularClassSymbol }


    private fun FirBasedSymbol<*>.addMustUseValueAnnotation(existingAnnotations: List<FirAnnotation>): List<FirAnnotation> {
        fun FirAnnotation.isMustUseReturnValue(): Boolean =
            toAnnotationClassId(session) == mustUseReturnValueClassId

        if (existingAnnotations.any(FirAnnotation::isMustUseReturnValue)) return existingAnnotations


        val muvClassSymbol = mustUseValueClass ?: return existingAnnotations
        val mustUseValueCtor = muvClassSymbol.primaryConstructorIfAny(session) ?: return existingAnnotations

        val annCall = buildAnnotationCall {
            argumentList = FirEmptyArgumentList
            annotationTypeRef = buildResolvedTypeRef {
                coneType = muvClassSymbol.defaultType()
            }
            calleeReference = buildResolvedNamedReference {
                name = muvClassSymbol.name
                resolvedSymbol = mustUseValueCtor
            }
            annotationResolvePhase = FirAnnotationResolvePhase.CompilerRequiredAnnotations
            source = this@addMustUseValueAnnotation.source
            // origin = TODO: Do we need special kind of origin or .Source is enough here?

            containingDeclarationSymbol = this@addMustUseValueAnnotation
        }
        return existingAnnotations + annCall
    }

    companion object {
        fun createIfNeeded(session: FirSession): FirMustUseValuePlacementTransformer? =
            if (session.languageVersionSettings.getFlag(AnalysisFlags.returnValueCheckerMode) == ReturnValueCheckerMode.FULL)
                FirMustUseValuePlacementTransformer(session)
            else null
    }
}
