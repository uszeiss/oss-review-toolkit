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
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.Instant

/**
 * The summary of a single run of the evaluator.
 */
@JacksonXmlRootElement(
    namespace = "spdx",
    localName = "CreationInfo"
)
data class SpdxCreationInfo(
    /**
     * Version of SPDX license list (spdx.org/licenses) used in the document.
     * Cardinality: Optional, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "spdx",
        localName = "LicenseListVersion"
    )
    @JsonProperty("LicenseListVersion")
    val licenseListVersion: String? = null,

    /**
     * The list of [SpdxCreator]s who created the document.
     * Cardinality: Mandatory, one or many.
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonUnwrapped
    val creators: List<SpdxCreator>,

    /**
     * Comment on document creation from its creator(s).
     * Cardinality: Optional, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "rdfs",
        localName = "comment"
    )
    @JsonProperty("CreatorComment")
    val creatorComment: String? = null
) {
    /**
     * The date and time [Instant] the document was created.
     * Cardinality: Mandatory, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "spdx",
        localName = "created"
    )
    @JsonProperty("Created")
    var created: Instant = Instant.EPOCH
        private set
}
