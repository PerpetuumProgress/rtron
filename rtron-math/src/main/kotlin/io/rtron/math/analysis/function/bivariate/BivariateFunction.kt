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

package io.rtron.math.analysis.function.bivariate

import com.github.kittinunf.result.Result
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyContains


/**
 * Function with exactly two parameters of the form z = f(x, y).
 */
abstract class BivariateFunction {

    // initializers and properties

    /** function's domain for x */
    abstract val domainX: Range<Double>

    /** function's domain for y */
    abstract val domainY: Range<Double>

    // Methods
    internal abstract fun valueUnbounded(x: Double, y: Double): Result<Double, Exception>

    /**
     * Returns the value z = f(x, y). If [x] is not in [domainX] or [y] is not in [domainY] an error is returned.
     *
     * @param x parameter x for the function evaluation
     * @param y parameter y for the function evaluation
     */
    fun value(x: Double, y: Double): Result<Double, Exception> {
        return if (x in domainX && y in domainY) valueUnbounded(x, y)
        else Result.error(IllegalArgumentException(
                "Value x=$x must be within in the defined $domainX and value y=$y within $domainY."))
    }

    /**
     * Returns the value z = f(x, y). If [x] is not in [domainX] or [y] is not in [domainY] an error is returned.
     * However, a fuzziness is allowed with a certain [tolerance].
     *
     * @param x parameter x for the function evaluation
     * @param y parameter y for the function evaluation
     * @param tolerance allowed tolerance for fuzzy contains evaluation
     */
    fun valueInFuzzy(x: Double, y: Double, tolerance: Double): Result<Double, Exception> {
        return if (!domainX.fuzzyContains(x, tolerance) || !domainY.fuzzyContains(y, tolerance))
            Result.error(IllegalArgumentException(
                    "Value x=$x must be within in the defined $domainX and value y=$y within $domainY."))
        else valueUnbounded(x, y)
    }
}
