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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * A class describing a the type of [SpdxAnnotation].
 */
@JacksonXmlRootElement(
    localName = "spdx:annotationType"
)
enum class SpdxAnnotationType {
    /**
     * Type of annotation which does not fit in any of the pre-defined annotation types.
     */
    OTHER,

    /**
     * A Review represents an audit and signoff by an individual, organization
     * or tool on the information for an SpdxElement.
     */
    REVIEW;
}
