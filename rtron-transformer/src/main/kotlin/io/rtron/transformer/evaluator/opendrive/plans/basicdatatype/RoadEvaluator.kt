/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import arrow.core.None
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.road.lateral.RoadLateralProfileShape
import io.rtron.std.filterToStrictSortingBy
import io.rtron.std.isSortedBy
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluatorParameters
import io.rtron.transformer.evaluator.opendrive.modifiers.BasicDataTypeModifier
import io.rtron.transformer.messages.opendrive.of

object RoadEvaluator {

    // Methods
    fun evaluate(opendriveModel: OpendriveModel, parameters: OpendriveEvaluatorParameters, messageList: DefaultMessageList): OpendriveModel {
        var modifiedOpendriveModel = opendriveModel.copy()

        everyRoad.modify(modifiedOpendriveModel) { currentRoad ->

            if (currentRoad.planView.geometry.isEmpty())
                messageList += DefaultMessage.of("NoPlanViewGeometryElements", "Plan view of road does not contain any geometry elements.", currentRoad.additionalId, Severity.FATAL_ERROR, wasFixed = false)

            if (currentRoad.lanes.laneSection.isEmpty())
                messageList += DefaultMessage.of("NoLaneSections", "Road does not contain any lane sections.", currentRoad.additionalId, Severity.FATAL_ERROR, wasFixed = false)

            currentRoad
        }

        modifiedOpendriveModel = everyRoad.modify(modifiedOpendriveModel) { currentRoad ->

            if (currentRoad.elevationProfile.exists { it.elevation.isEmpty() }) {
                messageList += DefaultMessage.of("NoElevationProfileElements", "Elevation profile contains no elements.", currentRoad.additionalId, Severity.WARNING, wasFixed = true)
                currentRoad.elevationProfile = None
            }

            currentRoad.elevationProfile.tap { elevationProfile ->
                elevationProfile.elevation = BasicDataTypeModifier.filterToStrictlySorted(elevationProfile.elevation, { it.s }, currentRoad.additionalId, "elevation", messageList)
            }

            currentRoad.lateralProfile.tap { currentLateralProfile ->
                if (currentLateralProfile.containsShapeProfile() && currentRoad.lanes.containsLaneOffset()) {
                    messageList += DefaultMessage.of("UnexpectedValue", "Unexpected value for attribute 'lateralProfile.shape'", currentRoad.additionalId, Severity.WARNING, wasFixed = true)
                    if (!parameters.skipRoadShapeRemoval)
                        currentLateralProfile.shape = emptyList()
                }

                currentLateralProfile.superelevation = BasicDataTypeModifier.filterToStrictlySorted(currentLateralProfile.superelevation, { it.s }, currentRoad.additionalId, "superelevation", messageList)
                currentLateralProfile.shape = BasicDataTypeModifier.filterToSorted(currentLateralProfile.shape, { it.s }, currentRoad.additionalId, "shape", messageList)

                val shapeEntriesFilteredByT: List<RoadLateralProfileShape> = currentLateralProfile.shape.groupBy { it.s }.flatMap { currentShapeSubEntries ->
                    currentShapeSubEntries.value.filterToStrictSortingBy { it.t }
                }
                if (shapeEntriesFilteredByT.size < currentLateralProfile.shape.size) {
                    // OpendriveException.NonStrictlySortedList("shape", "Ignoring ${it.shape.size - shapeEntriesFilteredByT.size} shape entries which are not placed in ascending order according to t for each s group.").toMessage(currentRoad.additionalId, isFatal = false, wasFixed = true)
                    messageList += DefaultMessage.of("NonStrictlySortedList", "Ignoring ${currentLateralProfile.shape.size - shapeEntriesFilteredByT.size} shape entries which are not placed in ascending order according to t for each s group.", currentRoad.additionalId, Severity.WARNING, wasFixed = true)
                    currentLateralProfile.shape = shapeEntriesFilteredByT
                }
            }

            currentRoad.lanes.laneOffset = BasicDataTypeModifier.filterToStrictlySorted(currentRoad.lanes.laneOffset, { it.s }, currentRoad.additionalId, "shape", messageList)

            if (!currentRoad.lanes.laneSection.isSortedBy { it.s }) {
                messageList += DefaultMessage.of("NonSortedList", "Sorting lane sections according to s.", currentRoad.additionalId, Severity.WARNING, wasFixed = true)
                currentRoad.lanes.laneSection = currentRoad.lanes.laneSection.sortedBy { it.s }
            }

            currentRoad
        }

        return modifiedOpendriveModel
    }
}
