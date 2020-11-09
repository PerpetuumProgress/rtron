/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.io.logging

import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration


/**
 * Progress bar in the console.
 *
 * @param taskName shown name of task at the beginning of the progress bar
 * @param completion maximum steps to until the task is completed
 * @param currentStatus current progress status
 */
@OptIn(ExperimentalTime::class)
class ProgressBar(
        private val taskName: String,
        private val completion: Int,
        private var currentStatus: Int = 0
) {

    // Properties and Initializers
    private val startTime = System.currentTimeMillis()
    private var lastPrintUpdateTime: Long = 0

    // Methods
    /** Increments progress bar by one step. */
    fun step() {
        currentStatus += 1
        printUpdate()
    }

    /** Jumps to step [n]. */
    fun stepTo(n: Int) {
        currentStatus = n
        printUpdate()
    }

    private fun printUpdate() {
        val elapsedTime = getElapsedTime()
        if (elapsedTime > PRINT_AFTER && getElapsedTimeSinceLastUpdate() > PRINT_AT_LEAST) {
            print("$PREFIX $taskName $currentStatus/$completion ${getProgressPercent()}% " +
                    "[ET $elapsedTime, ETA ${getEstimatedTimeOfArrival()}]\r")
            lastPrintUpdateTime = System.currentTimeMillis()
        }
    }

    private fun getElapsedTimeSinceLastUpdate(): Duration =
            (System.currentTimeMillis() - lastPrintUpdateTime).toDuration(DurationUnit.MILLISECONDS)

    private fun getProgressPercent(): Int =
            (100.0 * (currentStatus.toFloat() / completion.toFloat())).roundToInt()

    private fun getElapsedTime(): Duration =
            (System.currentTimeMillis() - startTime).toDuration(DurationUnit.MILLISECONDS)

    private fun getTotalEstimatedElapsedTime(): Duration =
            getElapsedTime() * completion / currentStatus

    private fun getEstimatedTimeOfArrival(): Duration =
            getElapsedTime() * ((completion / currentStatus) - 1)

    companion object {

        /**
         * Prefix of the progress bar.
         */
        const val PREFIX = "Progress:"

        /**
         * Starts printing the progress bar after the duration of [PRINT_AFTER].
         */
        val PRINT_AFTER = 0.toDuration(DurationUnit.SECONDS)

        /**
         * Print updates only after the duration of [PRINT_AT_LEAST] has elapsed.
         */
        val PRINT_AT_LEAST = 0.toDuration(DurationUnit.SECONDS)
    }
}
