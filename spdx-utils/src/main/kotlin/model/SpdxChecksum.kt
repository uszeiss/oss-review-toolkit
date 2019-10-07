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

package com.here.ort.spdx.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * Checksum to be able to verify provided [SpdxDocument], [SpdxPackage] or [SpdxFile] against actual files.
 */
@JacksonXmlRootElement(
    localName = "spdx:Checksum"
)
@JsonRootName(value = "Checksum")
data class SpdxChecksum(
    /**
     * Type of the [SpdxAnnotation]
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
        isAttribute = true,
        namespace = "rdf",
        localName = "resource"
    )
    val algorithm: SpdxChecksumAlgorithm,

    /**
     * Value of the [SpdxChecksum].
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "spdx",
        localName = "checksumValue"
    )
    val value: String

    ) : Comparable<SpdxChecksum> {
    /**
     * A comparison function to sort [SpdxChecksum]s.
     */
    override fun compareTo(other: SpdxChecksum) =
        compareValuesBy(
            this,
            other,
            compareBy(SpdxChecksum::value)
                .thenBy(SpdxChecksum::algorithm)
        ) { it }
}
