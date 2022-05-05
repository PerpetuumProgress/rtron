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

package io.rtron.model.opendrive.road.elevation

import arrow.core.NonEmptyList
import arrow.core.Validated
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.std.filterToStrictSortingBy
import io.rtron.std.toValidated

data class RoadElevationProfile(
    var elevation: List<RoadElevationProfileElevation> = emptyList(),
) : OpendriveElement() {

    val elevationValidated: Validated<OpendriveException.EmptyList, NonEmptyList<RoadElevationProfileElevation>>
        get() = NonEmptyList.fromList(elevation).toValidated { OpendriveException.EmptyList("elevation") }

    fun healMinorViolations(): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        val elevationEntriesFiltered = elevation.filterToStrictSortingBy { it.s }
        if (elevationEntriesFiltered.size < elevation.size) {
            healedViolations += OpendriveException.NonStrictlySortedList("elevation", "Ignoring ${elevation.size - elevationEntriesFiltered.size} elevation entries which are not placed in strict order according to s.")
            elevation = elevationEntriesFiltered
        }

        return healedViolations
    }
}
