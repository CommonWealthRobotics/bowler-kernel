/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexEDRMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultVexWheel
import com.neuronrobotics.bowlerkernel.vitamins.vitaminsupplier.gitvitaminsupplier.KlaxonGitVitamin
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktguava.klaxon.immutableMapConverter
import kotlin.reflect.KClass

/**
 * Finds all nested classes in a sealed hierarchy. All subclasses must be nested inside the
 * [parent].
 *
 * @param parent The top-level class that forms the sealed hierarchy.
 * @return All nested subclasses. Does not include the [parent].
 */
fun <B : Any, T : B> allNestedSubclasses(parent: KClass<T>): List<KClass<out T>> {
    @Suppress("UNCHECKED_CAST")
    val nestedClasses = parent.nestedClasses as Collection<KClass<T>>
    return nestedClasses + nestedClasses.flatMap {
        allNestedSubclasses(it)
    }
}

/**
 * Creates and configures a [Klaxon] instance with the default converters.
 */
fun getConfiguredKlaxon() = getConfiguredKlaxonWithoutGitVitaminConverter().apply {
    converter(KlaxonGitVitamin)
}

/**
 * Creates and configures a [Klaxon] instance with the default converters and without the
 * [KlaxonGitVitamin] converter.
 */
fun getConfiguredKlaxonWithoutGitVitaminConverter() = Klaxon().apply {
    fieldConverter(ConvertImmutableMap::class, immutableMapConverter())
    addSealedObjectHierarchyConverters()
}

/**
 * All sealed object hierarchies that [Klaxon] needs to parse into.
 */
val sealedObjectHierarchies = immutableListOf(
    DefaultVexWheel::class,
    VexEDRMotor::class
)

/**
 * Adds a converter for the [sealedObjectHierarchies].
 *
 * @receiver The [Klaxon] instance to add the converters to.
 */
fun Klaxon.addSealedObjectHierarchyConverters() = apply {
    sealedObjectHierarchies.forEach {
        converter(SealedObjectHierarchyConverter(it))
    }
}
