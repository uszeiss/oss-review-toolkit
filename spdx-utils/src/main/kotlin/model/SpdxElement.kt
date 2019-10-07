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

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import java.util.*

/**
 * A generic class describing a the type of [SpdxAnnotation].
 */
@JacksonXmlRootElement(
    localName = "spdx:SpdxElement"
)
data class SpdxElement(
    /**
     * Reference to the element.
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "rdf",
        localName = "about"
    )
    @JsonIgnore
    val name: String,

    /**
     * Set of [SpdxAnnotation]s belong to [SpdxElement].
     * Cardinality: Optional, one or many.
     */
    @JacksonXmlText
    @JsonIgnore
    val annotations: SortedSet<Annotation> = sortedSetOf(),

    /**
     * Set of [SpdxRelationship]s belong to [SpdxElement].
     * Cardinality: Optional, one or many.
     */
    @JacksonXmlText
    @JsonIgnore
    val relationships: SortedSet<SpdxRelationship> = sortedSetOf(),

    /**
     * Comments.
     * Cardinality: Optional, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "rdf"
    )
    @JsonIgnore
    val comment: String

    ) : Comparable<SpdxElement> {

    companion object {
        /**
         * A constant for an [SpdxElement] where all properties are empty.
         */
        @JvmField
        val EMPTY = SpdxElement(
            annotations = sortedSetOf(),
            comment = "",
            name = "",
            relationships = sortedSetOf()
        )
    }

    /**
     * A comparison function to sort [spdxElement]s.
     */
    override fun compareTo(other: SpdxElement) = name.compareTo(other.name)
}
