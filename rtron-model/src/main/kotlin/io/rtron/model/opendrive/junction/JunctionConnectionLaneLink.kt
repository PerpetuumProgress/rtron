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

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.core.OpendriveElement

data class JunctionConnectionLaneLink(
    var from: Int = Int.MIN_VALUE,
    var to: Int = Int.MIN_VALUE
) : OpendriveElement() {

    // Properties and Initializers
    val fromValidated: Validated<OpendriveException.MissingValue, Int>
        get() = if (from == Int.MIN_VALUE) OpendriveException.MissingValue("").invalid() else from.valid()
    val toValidated: Validated<OpendriveException.MissingValue, Int>
        get() = if (to == Int.MIN_VALUE) OpendriveException.MissingValue("").invalid() else to.valid()

    // Methods
    fun getFatalViolations(): List<OpendriveException> =
        fromValidated.fold({ listOf(it) }, { emptyList() }) +
            toValidated.fold({ listOf(it) }, { emptyList() })
}
