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

package io.rtron.transformer.evaluator.opendrive.plans.modelingrules

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.io.report.merge
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.plans.AbstractOpendriveEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.junction.JunctionEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.lane.RoadLanesEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.objects.RoadObjectsEvaluator
import io.rtron.transformer.evaluator.opendrive.plans.modelingrules.road.RoadEvaluator

class ModelingRulesEvaluator(val configuration: OpendriveEvaluatorConfiguration) :
    AbstractOpendriveEvaluator() {

    // Properties amd Initializers
    private val _roadEvaluator = RoadEvaluator(configuration)
    private val _roadLanesEvaluator = RoadLanesEvaluator(configuration)
    private val _roadObjectsEvaluator = RoadObjectsEvaluator(configuration)
    private val _junctionEvaluator = JunctionEvaluator(configuration)

    // Methods
    override fun evaluateFatalViolations(opendriveModel: OpendriveModel): Report {
        val report = Report()

        report += _roadEvaluator.evaluateFatalViolations(opendriveModel.road)
        report += opendriveModel.road.map { _roadLanesEvaluator.evaluateFatalViolations(RoadIdentifier(it.id), it.lanes, it.length) }.merge()
        report += opendriveModel.road.map { _roadObjectsEvaluator.evaluateFatalViolations(RoadIdentifier(it.id), it.objects) }.merge()
        report += _junctionEvaluator.evaluateFatalViolations(opendriveModel.junction)

        return report
    }

    override fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextReport<OpendriveModel> {
        val healedOpendriveModel = opendriveModel.copy()
        val report = Report()

        _roadEvaluator.evaluateNonFatalViolations(healedOpendriveModel.road).let {
            healedOpendriveModel.road = it.value
            report += it.report
        }

        healedOpendriveModel.road.forEach { currentRoad ->
            val roadIdentifier = RoadIdentifier(currentRoad.id)

            _roadLanesEvaluator.evaluateNonFatalViolations(roadIdentifier, currentRoad.lanes, currentRoad.length).let {
                currentRoad.lanes = it.value
                report += it.report
            }

            _roadObjectsEvaluator.evaluateNonFatalViolations(roadIdentifier, currentRoad.objects).let {
                currentRoad.objects = it.value
                report += it.report
            }
        }

        _junctionEvaluator.evaluateNonFatalViolations(healedOpendriveModel.junction).let {
            healedOpendriveModel.junction = it.value
            report += it.report
        }

        return ContextReport(healedOpendriveModel, report)
    }
}
