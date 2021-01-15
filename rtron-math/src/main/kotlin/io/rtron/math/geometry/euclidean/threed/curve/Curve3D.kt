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

package io.rtron.math.geometry.euclidean.threed.curve

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.pure.LinearFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.curve.CurveRelativeLineSegment3D
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.Pose3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.twod.curve.AbstractCurve2D
import io.rtron.math.range.fuzzyContainsResult
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.transform.Affine3D
import io.rtron.std.handleFailure


/**
 * A curve in 3D defined by a curve in 2D and a height function. Furthermore, the curve can have a torsion, which is
 * relevant for pose and transformation matrix calculations along the curve.
 * See the wikipedia article on [torsion of a curve](https://en.wikipedia.org/wiki/Torsion_of_a_curve).
 *
 * @param curveXY the curve in the xy plane
 * @param heightFunction the definition of the height, which must be defined where the [curveXY] is defined
 * @param torsionFunction the torsion of the curve, which must be defined where the [curveXY] is defined
 */
data class Curve3D(
        val curveXY: AbstractCurve2D,
        val heightFunction: UnivariateFunction,
        val torsionFunction: UnivariateFunction = LinearFunction.X_AXIS
) : AbstractCurve3D() {

    // Properties and Initializers
    init {
        require(heightFunction.domain.fuzzyEncloses(curveXY.domain, tolerance))
        { "The height function must be defined everywhere where the curveXY is also defined." }
        require(torsionFunction.domain.fuzzyEncloses(curveXY.domain, tolerance))
        { "The torsion function must be defined everywhere where the curveXY is also defined." }
    }

    override val domain get() = curveXY.domain
    override val tolerance get() = curveXY.tolerance

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Result<Vector3D, Exception> {

        val pointXY = curveXY.calculatePointGlobalCS(curveRelativePoint)
                .handleFailure { throw it.error }
        val height = heightFunction.valueInFuzzy(curveRelativePoint.curvePosition, tolerance)
                .handleFailure { throw it.error }
        val vector = Vector3D(pointXY.x, pointXY.y, height)
        return Result.success(vector)
    }

    /**
     * Returns a pose at the position along the curve [curveRelativePoint].
     *
     * @param curveRelativePoint pose is calculated on the [curveRelativePoint]
     * @return pose whereby the orientation is tangential to this curve and its torsion
     */
    fun calculatePose(curveRelativePoint: CurveRelativeVector1D): Result<Pose3D, Exception> {
        this.domain.fuzzyContainsResult(curveRelativePoint.curvePosition, tolerance).handleFailure { return it }

        val poseXY = curveXY.calculatePoseGlobalCS(curveRelativePoint)
                .handleFailure { throw it.error }
        val height = heightFunction.value(curveRelativePoint.curvePosition)
                .handleFailure { throw it.error }
        val torsion = torsionFunction.value(curveRelativePoint.curvePosition)
                .handleFailure { throw it.error }

        val point = Vector3D(poseXY.point.x, poseXY.point.y, height)
        val rotation = Rotation3D(poseXY.rotation.angle, 0.0, torsion)

        return Result.success(Pose3D(point, rotation))
    }

    /**
     * Returns an [Affine3D] at the position along the curve [curveRelativePoint].
     *
     * @param curveRelativePoint affine transformation matrix is calculated on the [curveRelativePoint]
     * @return affine transformation matrix whereby the orientation is tangential to this curve and its torsion
     */
    fun calculateAffine(curveRelativePoint: CurveRelativeVector1D): Result<Affine3D, Exception> =
            calculatePose(curveRelativePoint).map { Affine3D.of(it) }

    /**
     * Transforms the [curveRelativePoint] (relative to this curve) to a [Vector3D] in cartesian coordinates.
     *
     * @param curveRelativePoint point in curve relative coordinates
     * @return point in cartesian coordinates
     */
    fun transform(curveRelativePoint: CurveRelativeVector3D): Result<Vector3D, Exception> {
        val affine = calculateAffine(curveRelativePoint.toCurveRelative1D()).handleFailure { return it }
        val vector = affine.transform(curveRelativePoint.getCartesianCurveOffset())
        return Result.success(vector)
    }

    /**
     * Transforms the [curveRelativeLineSegment] (relative to this curve) to a [LineSegment3D] in cartesian coordinates.
     *
     * @param curveRelativeLineSegment line segment in curve relative coordinates
     * @return line segment in cartesian coordinates
     */
    fun transform(curveRelativeLineSegment: CurveRelativeLineSegment3D): Result<LineSegment3D, Exception> {
        val start = transform(curveRelativeLineSegment.start).handleFailure { return it }
        val end = transform(curveRelativeLineSegment.end).handleFailure { return it }
        return LineSegment3D.of(start, end, curveRelativeLineSegment.tolerance, endBoundType)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Curve3D) return false

        if (curveXY != other.curveXY) return false
        if (heightFunction != other.heightFunction) return false
        if (torsionFunction != other.torsionFunction) return false
        if (tolerance != other.tolerance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = curveXY.hashCode()
        result = 31 * result + heightFunction.hashCode()
        result = 31 * result + torsionFunction.hashCode()
        result = 31 * result + tolerance.hashCode()
        return result
    }
}
