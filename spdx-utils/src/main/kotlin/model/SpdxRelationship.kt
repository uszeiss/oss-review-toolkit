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
 * Relationship between two SPDX documents, packages or files.
 */
@JacksonXmlRootElement(
    localName = "spdx:relationship"
)
data class SpdxRelationship(
    /**
     * Comments from relationship creator.
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
        localName = "spdx:comment"
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val comment: String? = null,

    /**
     * Type of the [SpdxRelationship]
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
        localName = "spdx:relationshipType"
    )
    @JsonProperty("relationshipType")
    val type: SpdxRelationshipType,

    /**
     * Type of the [SpdxRelationship]
     * Cardinality: Mandatory, one.
     */
    @get:JacksonXmlProperty(
        localName = "spdx:relatedSpdxElementId"
    )
    val relatedSpdxElementId: SpdxElement

    ) : Comparable<SpdxRelationship> {

    /**
     * A comparison function to sort [SpdxRelationship]s.
     */
    override fun compareTo(other: SpdxRelationship) = type.compareTo(other.type)
}
