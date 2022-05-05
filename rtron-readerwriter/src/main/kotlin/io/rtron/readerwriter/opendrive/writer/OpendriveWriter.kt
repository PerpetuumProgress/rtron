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

package io.rtron.readerwriter.opendrive

import arrow.core.Either
import arrow.core.computations.either
import io.rtron.io.files.Path
import io.rtron.io.logging.LogManager
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.opendrive.configuration.OpendriveWriterConfiguration
import io.rtron.readerwriter.opendrive.writer.OpendriveMarshaller
import io.rtron.std.BaseException

class OpendriveWriter(
    val configuration: OpendriveWriterConfiguration
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)

    private val _opendriveMarshaller by lazy { OpendriveMarshaller() }

    // Methods
    fun write(model: OpendriveModel, directoryPath: Path): Either<OpendriveWriterException, Path> = either.eager {

        val filePath = _opendriveMarshaller.writeToFile(model, directoryPath).bind()
        _reportLogger.info("Completed writing of file ${filePath.fileName} (around ${filePath.getFileSizeToDisplay()}). ✔")

        filePath
    }
}

sealed class OpendriveWriterException(message: String) : BaseException(message)
