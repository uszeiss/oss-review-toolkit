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
import java.util.*

/**
 * Annotation on a [SpdxDocument], [SpdxFile], or [SpdxPackage].
 */
@JacksonXmlRootElement(
    localName = "spdx:Annotation"
)
data class SpdxAnnotation(
    /**
     * This field identifies the person, organization or tool that has commented on a file, package, or entire document.
     * Cardinality: Mandatory, one.
     */
    @get:JacksonXmlProperty(
        localName = "spdx:annotator"
    )
    val annotator: String,

    /**
     * Identify when the comment was made.
     * This is to be specified according to the combined date and time in the UTC format,
     * as specified in the ISO 8601 standard.
     * Cardinality: Mandatory, one.
     */
    @get:JacksonXmlProperty(
      localName = "spdx:annotationDate"
    )
    @get:JsonProperty("annotationDate")
    @get:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    val date: Date,

    /**
     * Type of the annotation.
     * Cardinality: Mandatory, one.
     */
    @get:JacksonXmlProperty(
        localName = "spdx:annotationType"
    )
    @get:JsonProperty("annotationType")
    val type: SpdxAnnotationType,

    /**
     * Comments from Annotator.
     * Cardinality: Mandatory, one.
     */
    @get:JacksonXmlProperty(
        localName = "spdx:comment"
    )
    @get:JsonProperty("annotationComment")
    val comment: String

    ) : Comparable<SpdxAnnotation> {

    /**
     * A comparison function to sort [SpdxAnnotation]s.
     */
    override fun compareTo(other: SpdxAnnotation) =
        compareValuesBy(
            this,
            other,
            compareBy(SpdxAnnotation::date)
                .thenBy(SpdxAnnotation::type)
                .thenBy(SpdxAnnotation::annotator)
                .thenBy(SpdxAnnotation::comment)
        ) { it }
}

