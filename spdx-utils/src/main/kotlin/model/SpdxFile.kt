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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * Describes a software file
 */
@JacksonXmlRootElement(
    localName = "spdx:File"
)
data class SpdxFile(
    /**
     * Name as given by package originator.
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        localName = "spdx:name"
    )
    val name: String,

    /**
     * Identifier for the package.
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
      localName = "spdx:SPDXID"
    )
    @JsonProperty("SPDXID")
    val id: String,

//    /**
//     */
//    @JacksonXmlProperty(
//        localName = "spdx:fileTypes"
//    )
//    @JsonProperty("fileTypes")
//    val type: String? = null,
//
//    /**
//     */
//    @JacksonXmlProperty(
//        localName = "spdx:fileCheckSum"
//    )
//    @JsonProperty("fileChecksum")
//    val checksum: String? = null,
//
//    /**
//     */
//    @JacksonXmlProperty(
//        localName = "spdx:fileComment"
//    )
//    @JsonProperty("fileComment")
//    val comment: String? = null,
//
//    /**
//     */
//    @JacksonXmlProperty(
//        localName = "spdx:fileContributors"
//    )
//    @JsonProperty("fileContributors")
//    val fileContributors: String? = null,

//    /**
//     */
//    @JacksonXmlProperty(
//        namespace = "spdx:licenseConcluded"
//    )
//    val licenseConcluded: String? = null,

    /**
     */
    @JacksonXmlElementWrapper(
        useWrapping = false
    )
    @JacksonXmlProperty(
        localName = "spdx:licenseInfoFromFiles"
    )
    val licenseInfoFromFiles: List<String>,

    /**
     */
    @JacksonXmlProperty(
        namespace = "spdx:licenseComments"
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val licenseComments: String? = null

    ) : Comparable<SpdxFile> {
    companion object {
        /**
         * A constant for a [SpdxPackage] where all properties are empty.
         */
        @JvmField
        val EMPTY = SpdxFile(
            name = "",
            id = "",
            licenseInfoFromFiles = emptyList()
        )
    }

    /**
     * A comparison function to sort files by their SPDX id.
     */
    override fun compareTo(other: SpdxFile) = id.compareTo(other.id)
}
