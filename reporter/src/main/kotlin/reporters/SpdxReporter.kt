/*
 * Copyright (C) 2019 HERE Europe B.V.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.ort.reporter.reporters

import com.fasterxml.jackson.databind.ObjectMapper

import com.here.ort.reporter.Reporter
import com.here.ort.reporter.ReporterInput
import com.here.ort.spdx.model.SpdxDocument
import com.here.ort.spdx.model.SpdxDocumentDescribes
import com.here.ort.spdx.model.SpdxFile
import com.here.ort.spdx.model.SpdxPackage
import com.here.ort.utils.toHexString

import java.io.OutputStream
import java.security.MessageDigest

/**
 * TODO
 */
abstract class SpdxReporter(val mapper: ObjectMapper) : Reporter {
    override fun generateReport(outputStream: OutputStream, input: ReporterInput) {
        val licenseFindings = input.ortResult.collectLicenseFindings()

        val packages = mutableListOf<SpdxPackage>()

        input.ortResult.analyzer?.result?.projects?.forEach { project ->
            val findings = licenseFindings[project.id].orEmpty().keys
            val allFiles = findings.flatMap { it.locations }.map { it.path }.toSortedSet()

            val spdxFiles = allFiles.map { path ->
                val id = MessageDigest.getInstance("SHA-1").digest(path.toByteArray()).toHexString()
                val licenses = findings.filter { it.locations.any { it.path == path } }.map { it.license }

                SpdxFile(
                    name = "./$path",
                    id = "SPDXRef-File-$id",
                    licenseInfoFromFiles = licenses,
                    licenseComments = null
                )
            }

            val spdxPkg = SpdxPackage(
                name = project.id.name,
                id = project.id.toCoordinates(),
                version = project.id.version,
                fileName = null,
                supplier = null,
                originator = null,
                files = spdxFiles,
                filesAnalyzed = true
            )

            packages += spdxPkg
        }

        input.ortResult.analyzer?.result?.packages?.forEach { pkg ->
            val findings = licenseFindings[pkg.pkg.id].orEmpty().keys
            val allFiles = findings.flatMap { it.locations }.map { it.path }.toSortedSet()

            val spdxFiles = allFiles.map { path ->
                val id = MessageDigest.getInstance("SHA-1").digest(path.toByteArray()).toHexString()
                val licenses = findings.filter { it.locations.any { it.path == path } }.map { it.license }

                SpdxFile(
                    name = "./$path",
                    id = "SPDXRef-File-$id",
                    licenseInfoFromFiles = licenses,
                    licenseComments = null
                )
            }

            val spdxPkg = SpdxPackage(
                name = pkg.pkg.id.name,
                id = pkg.pkg.id.toCoordinates(),
                version = pkg.pkg.id.version,
                fileName = null,
                supplier = null,
                originator = null,
                files = spdxFiles,
                filesAnalyzed = true
            )

            packages += spdxPkg
        }

        val documentDescribes = SpdxDocumentDescribes(
            files = emptyList(),
            packages = packages
        )

        val spdx = SpdxDocument(
            id = "id",
            name = "name",
            licenseListVersion = "licenseListVersion",
            creatorComment = "creatorComment",
            documentDescribes = documentDescribes
        )

        outputStream.use {
            mapper.writerWithDefaultPrettyPrinter().writeValue(it, spdx)
        }
    }
}
