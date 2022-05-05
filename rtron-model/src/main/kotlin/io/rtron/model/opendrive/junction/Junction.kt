/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rtron.model.opendrive.junction

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Validated
import arrow.core.getOrElse
import arrow.core.invalid
import arrow.core.valid
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.objects.EOrientation
import io.rtron.std.toValidated

data class Junction(
    var connection: List<JunctionConnection> = emptyList(),
    var priority: List<JunctionPriority> = emptyList(),
    var controller: List<JunctionController> = emptyList(),
    var surface: Option<JunctionSurface> = None,

    var id: String = "",
    var mainRoad: Option<String> = None,
    var name: Option<String> = None,
    var orientation: Option<EOrientation> = None,
    var sEnd: Option<Double> = None,
    var sStart: Option<Double> = None,
    var type: Option<EJunctionType> = None
) : OpendriveElement() {

    // Properties and Initializers
    val connectionValidated: Validated<OpendriveException.EmptyList, NonEmptyList<JunctionConnection>>
        get() = NonEmptyList.fromList(connection).toValidated { OpendriveException.EmptyList("connection") }

    val idValidated: Validated<OpendriveException.MissingValue, String>
        get() = if (id.isBlank()) OpendriveException.MissingValue("id").invalid() else id.valid()

    val typeValidated: EJunctionType
        get() = type.getOrElse { EJunctionType.DEFAULT }

    // Methods
    fun getConnectingRoadIds(): Set<String> = connection.flatMap { it.incomingRoad.toList() }.toSet()
    fun getIncomingRoadIds(): Set<String> = connection.flatMap { it.incomingRoad.toList() }.toSet()
    fun getNumberOfIncomingRoads(): Int = getIncomingRoadIds().size

    fun getSevereViolations(): List<OpendriveException> =
        connectionValidated.fold({ listOf(it) }, { emptyList() }) +
            idValidated.fold({ listOf(it) }, { emptyList() })

    fun healMinorViolations(): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        if (mainRoad.exists { it.isBlank() }) {
            mainRoad = None
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("mainRoad")
        }

        if (name.exists { it.isBlank() }) {
            name = None
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("name")
        }

        if (sEnd.exists { !it.isFinite() }) {
            sEnd = None
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("sEnd")
        }

        if (sStart.exists { !it.isFinite() }) {
            sStart = None
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("sStart")
        }

        return healedViolations
    }
}
