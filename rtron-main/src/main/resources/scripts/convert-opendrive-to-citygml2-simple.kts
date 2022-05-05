#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.2")
*/

import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlVersion

/**
 * This script converts OpenDRIVE datasets to CityGML2 by iterating over all files with the extension "xodr" in the
 * input directory.
 */
processAllFiles(
    inInputDirectory = "/project/input", // adjust path to directory of input datasets
    withExtension = "xodr",
    toOutputDirectory = "/project/output" // adjust path to output directory
)
{
    // Within this block the transformations can be defined by the user. For example:

    // 1. Read the OpenDRIVE dataset into memory:
    val opendriveModel = readOpendriveModel(inputFilePath)
        .fold( { logger.warn(it.message); return@processAllFiles }, { it })

    // 2. Transform the OpenDRIVE model to an intermediary representation (the RoadSpaces model):
    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        // Within this blocks, the transformation is parametrized:
        crsEpsg = 32632 // EPSG code of the coordinate reference system (obligatory for working with GIS applications)
    }.fold( { logger.warn(it.message); return@processAllFiles }, { it })

    // 3. Transform the RoadSpaces model to a CityGML model:
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        flattenGenericAttributeSets = true // true, if nested attribute lists shall be flattened out
        discretizationStepSize = 0.5 // distance between each discretization step for curves and surfaces
        generateRandomGeometryIds = false
    }

    // 4. Write the CityGML model to the output directory:
    writeCitygmlModel(citygmlModel) {
        versions = setOf(CitygmlVersion.V2_0) // set the CityGML versions for writing
    }
}
