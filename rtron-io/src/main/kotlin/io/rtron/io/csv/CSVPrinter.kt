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

package io.rtron.io.csv

import io.rtron.io.files.Path
import org.apache.commons.csv.CSVFormat.Builder
import java.io.Flushable
import java.nio.file.Files
import org.apache.commons.csv.CSVPrinter as CMCSVPrinter

/**
 * Writes CSV files.
 *
 * @param filePath path to the file to be written
 * @param header header of the csv file
 */
class CSVPrinter(filePath: Path, header: List<String>) : Flushable {

    // Properties and Initializers
    init {
        filePath.parent.createDirectory()
    }

    private val _writer = Files.newBufferedWriter(filePath.toPathN())
    private val _csvPrinter: CMCSVPrinter
    init {
        val csvFormat = Builder.create().setHeader(*header.toTypedArray()).build()
        _csvPrinter = CMCSVPrinter(_writer, csvFormat)
    }

    // Methods
    /**
     * Prints the given [values] as a single record.
     *
     * @param values values to write
     */
    fun printRecord(vararg values: Any) {
        _csvPrinter.printRecord(*values)
    }

    /**
     * Flushes the stream.
     */
    override fun flush() {
        _csvPrinter.flush()
    }
}
