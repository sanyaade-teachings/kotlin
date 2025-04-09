/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.types

import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.SimpleTypeMarker
import org.jetbrains.kotlin.types.model.TypeConstructorMarker

fun TypeSystemCommonBackendContext.computeExpandedTypeForInlineClass(inlineClassType: KotlinTypeMarker): KotlinTypeMarker? =
    computeExpandedTypeInner(inlineClassType, hashSetOf())

private fun TypeSystemCommonBackendContext.computeExpandedTypeInner(
    kotlinType: KotlinTypeMarker, visitedClassifiers: HashSet<TypeConstructorMarker>
): KotlinTypeMarker? {
    val classifier = kotlinType.typeConstructor()
    if (!visitedClassifiers.add(classifier)) return null

    val typeParameter = classifier.getTypeParameterClassifier()

    return when {
        typeParameter != null -> {
            val upperBound = typeParameter.getRepresentativeUpperBound()
            computeExpandedTypeInner(upperBound, visitedClassifiers)
                ?.let { expandedUpperBound ->
                    val upperBoundIsPrimitiveOrInlineClass =
                        upperBound.typeConstructor().isInlineClass() || upperBound is SimpleTypeMarker && upperBound.isPrimitiveType()
                    when {
                        expandedUpperBound is SimpleTypeMarker && expandedUpperBound.isPrimitiveType() &&
                                kotlinType.isNullableType() && upperBoundIsPrimitiveOrInlineClass -> upperBound.makeNullable()
                        expandedUpperBound.isNullableType() || !kotlinType.isMarkedNullable() -> expandedUpperBound
                        else -> expandedUpperBound.makeNullable()
                    }
                }
        }

        classifier.isInlineClass() -> {
            // kotlinType is the boxed inline class type
            val unsubstitutedUnderlyingType = kotlinType.getUnsubstitutedUnderlyingType()
            val underlyingType = when {
                // value class of the form <A> ValueClass(value: A) and <A> ValueClass(value: Array<A>)
                unsubstitutedUnderlyingType != null && isTypeParameterOrArrayOfTypeParameterType(unsubstitutedUnderlyingType) -> unsubstitutedUnderlyingType
                // value class of the form <A> ValueClass(value: T<A>) and regular case
                else -> kotlinType.getSubstitutedUnderlyingType() ?: unsubstitutedUnderlyingType
            } ?: return null
            val expandedUnderlyingType = computeExpandedTypeInner(underlyingType, visitedClassifiers) ?: return null
            when {
                !kotlinType.isNullableType() -> expandedUnderlyingType

                // Here inline class type is nullable. Apply nullability to the expandedUnderlyingType.

                // Nullable types become inline class boxes
                expandedUnderlyingType.isNullableType() -> kotlinType

                // Primitives become inline class boxes
                expandedUnderlyingType is SimpleTypeMarker && expandedUnderlyingType.isPrimitiveType() -> kotlinType

                // Non-null reference types become nullable reference types
                else -> expandedUnderlyingType.makeNullable()
            }
        }

        else -> kotlinType
    }
}

fun TypeSystemCommonBackendContext.isTypeParameterOrArrayOfTypeParameterType(type: KotlinTypeMarker): Boolean =
    type.typeConstructor().getTypeParameterClassifier() != null ||
            (type.isArrayOrNullableArray() && type.getArgument(0).getType()?.let { isTypeParameterOrArrayOfTypeParameterType(it) } == true)
