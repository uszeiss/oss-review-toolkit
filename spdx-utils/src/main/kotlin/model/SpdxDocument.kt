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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * Provides information for forward and backward compatibility to tools processing the SPDX document.
 * Cardinality: Mandatory, one.
 */
//@XmlSchema(
//    xmlns = {
//        @XmlNs(prefix = "spdx", namespaceURI = http://spdx.org/rdf/terms#),
//        @XmlNs(prefix = "rdf", namespaceURI = http://www.w3.org/1999/02/22-rdf-syntax-ns#)
//    },
//    elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED
//)
@JacksonXmlRootElement(
    localName = "spdx:Document"
)
data class SpdxDocument(
    /**
     * Identifier for the document.
     * Cardinality: mandatory, one.
     */
    @JacksonXmlProperty(
      localName = "spdx:SPDXID"
    )
    @JsonProperty("SPDXID")
    val id: String,

    /**
     * Name given to the document by its creator.
     * Cardinality: mandatory, one.
     */
     val name: String,

    /**
     * Namespace for the document as a unique absolute Uniform Resource Identifier (URI).
     * Cardinality: mandatory, one.
     */
//    @JacksonXmlProperty(
//        localName = "spdx:about"
//    )
//    @JsonProperty("DocumentNamespace")
//    val namespace: String,

    // FIXME Implement ExternalDocumentRef per
    // https://spdx.org/spdx-specification-21-web-version#h.h430e9ypa0j9
    /**
     * External SPDX documents referenced within this document.
     * Cardinality: Optional, one or many.
     */
    /*
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "spdx",
        localName = "ExternalDocumentRef"
    )
    @JsonProperty("ExternalDocumentRef")
    val externalDocumentRef: String?,
    */

    /**
     * External SPDX documents referenced within this document.
     * Cardinality: Optional, one or many.
     */
    @JacksonXmlProperty(
        localName = "spdx:licenseListVersion"
    )
    @JsonProperty("licenseListVersion")
    val licenseListVersion: String?,

    /**
     * Information on the creation of this document.
     */
//    val creationInfo: SpdxCreationInfo,

    /**
     * Comment from document creators to document consumers.
     * Cardinality: Optional, one.
     */
    @JacksonXmlProperty(
        localName = "spdx:comment"
    )
    @JsonProperty("comment")
    val creatorComment: String? = null,

    /**
     * Comment from document creators to document consumers.
     * Cardinality: Optional, one.
     */
    @JacksonXmlProperty(
        localName = "spdx:documentDescribes"
    )
    val documentDescribes: SpdxDocumentDescribes
) {
    /**
     * Reference number to applicable SPDX version, used to determine how to parse and interpret the document.
     * Cardinality: mandatory, one.
     */
    @JacksonXmlProperty(
        localName = "spdx:specVersion"
    )
    @JsonProperty("SPDXVersion")
    var version: String = "2.2"
        private set

    /**
     * License of this document which is per the SPDX specification must be CC0-1.0.
     * Cardinality: mandatory, one.
     */
    @JacksonXmlProperty(
        localName = "spdx:dataLicense"
    )
    @JsonProperty("dataLicense")
    var license: String = "CC0-1.0"
        private set
}
