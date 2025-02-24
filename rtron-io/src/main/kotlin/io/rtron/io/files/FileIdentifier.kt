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

package io.rtron.io.files

import arrow.core.Option
import arrow.core.some
import java.io.File
import java.nio.file.Path

interface FileIdentifierInterface {
    val fileName: Option<String>
    val fileExtension: Option<String>
    val filePath: Option<Path>
    val fileHashSha256: String
}

/**
 * Identifier of a file with hash checksum to verify data integrity.
 *
 * @param fileName actual name of the file
 * @param fileExtension extension of the file
 * @param filePath path to the file
 * @param fileHashSha256 hash of the file
 */
data class FileIdentifier(
    override val fileName: Option<String>,
    override val fileExtension: Option<String>,
    override val filePath: Option<Path>,
    override val fileHashSha256: String
) : FileIdentifierInterface {

    companion object {
        fun of(path: Path): FileIdentifier = of(path.toFile())
        fun of(file: File) = FileIdentifier(file.nameWithoutExtension.some(), file.extension.some(), file.toPath().some(), file.getHashSha256())
    }
}
