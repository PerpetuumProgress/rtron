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

package io.rtron.transformer.roadspace2citygml.adder

import com.github.kittinunf.result.Result
import io.rtron.io.logging.Logger
import io.rtron.transformer.roadspace2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspace2citygml.module.*
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.citygml.core.AbstractCityObject
import org.citygml4j.model.citygml.core.CityModel
import org.citygml4j.model.citygml.core.CityObjectMember
import io.rtron.model.roadspaces.roadspace.objects.RoadObjectType
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.std.handleFailure


/**
 * Adds [RoadspaceObject] classes (RoadSpaces model) to the [CityModel] (CityGML model).
 */
class RoadObjectAdder(
        val configuration: Roadspaces2CitygmlConfiguration
) {

    // Properties and Initializers
    private val _reportLogger: Logger = configuration.getReportLogger()

    private val _genericsModuleBuilder = GenericsModuleBuilder(configuration)
    private val _buildingModuleBuilder = BuildingModuleBuilder(configuration)
    private val _cityFurnitureModuleBuilder = CityFurnitureModuleBuilder(configuration)
    private val _transportationModuleBuilder = TransportationModuleBuilder(configuration)
    private val _vegetationModuleBuilder = VegetationModuleBuilder()

    private val _attributesAdder = AttributesAdder(configuration)


    // Methods

    /**
     * Adds a list of [srcRoadspaceObjects] (RoadSpaces model) to the [dstCityModel] (CityGML model).
     */
    fun addRoadspaceObjects(srcRoadspaceObjects: List<RoadspaceObject>, dstCityModel: CityModel) {
        srcRoadspaceObjects.forEach { addSingleRoadspaceObject(it, dstCityModel) }
    }

    private fun addSingleRoadspaceObject(srcRoadspaceObject: RoadspaceObject, dstCityModel: CityModel) {
        val geometryTransformer = createGeometryTransformer(srcRoadspaceObject)
        val abstractCityObject = createAbstractCityObject(srcRoadspaceObject, geometryTransformer)
                .handleFailure { _reportLogger.log(it); return }

        _attributesAdder.addIdName(srcRoadspaceObject.name, abstractCityObject)
        _attributesAdder.addAttributes(srcRoadspaceObject.attributes, abstractCityObject)

        val cityObjectMember = CityObjectMember(abstractCityObject)
        dstCityModel.addCityObjectMember(cityObjectMember)
    }

    private fun createGeometryTransformer(srcRoadspaceObject: RoadspaceObject): GeometryTransformer {
        require(srcRoadspaceObject.geometry.size == 1)
        val currentGeometricPrimitive = srcRoadspaceObject.geometry.first()

        return GeometryTransformer(_reportLogger, configuration.parameters)
                .also { currentGeometricPrimitive.accept(it) }
    }

    /**
     * Creates a city object (CityGML model) from the [RoadspaceObject] and it's geometry.
     * Contains the rules which determine the CityGML feature types from the [RoadspaceObject].
     *
     * @param srcRoadspaceObject road space object from the RoadSpaces model
     * @param geometryTransformer transformed geometry
     * @return city object (CityGML model)
     */
    private fun createAbstractCityObject(srcRoadspaceObject: RoadspaceObject, geometryTransformer: GeometryTransformer):
            Result<AbstractCityObject, Exception> {

        // based on object name
        if (srcRoadspaceObject.name == "bench")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "bus")
            return _transportationModuleBuilder.createTransportationComplex(geometryTransformer,
                    TransportationModuleBuilder.Feature.ROAD)
        if (srcRoadspaceObject.name == "controllerBox")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "crossWalk")
            return _transportationModuleBuilder.createTransportationComplex(geometryTransformer,
                    TransportationModuleBuilder.Feature.ROAD)
        if (srcRoadspaceObject.name == "fence")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "noParkingArea")
            return _transportationModuleBuilder.createTransportationComplex(geometryTransformer,
                    TransportationModuleBuilder.Feature.ROAD)
        if (srcRoadspaceObject.name == "railing")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "raiseMedian")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "trafficLight")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "trafficSign")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "unknown")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.name == "wall")
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)

        // based on object type
        if (srcRoadspaceObject.type == RoadObjectType.BARRIER)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.TREE)
            return _vegetationModuleBuilder.createVegetationObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.VEGETATION)
            return _vegetationModuleBuilder.createVegetationObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.BUILDING)
            return _buildingModuleBuilder.createBuildingObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.STREET_LAMP)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.SIGNAL)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)
        if (srcRoadspaceObject.type == RoadObjectType.POLE)
            return _cityFurnitureModuleBuilder.createCityFurnitureObject(geometryTransformer)

        // if no rule for object name and type, create a generic city object
        return _genericsModuleBuilder.createGenericObject(geometryTransformer)
    }

}
