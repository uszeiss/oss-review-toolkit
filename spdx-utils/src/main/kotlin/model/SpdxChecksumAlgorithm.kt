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

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * A class to denote the type of [SpdxChecksum].
 */
enum class SpdxChecksumAlgorithm (
    /**
     * The tag-value string for [SpdxChecksumAlgorithm].
     */
    val tag: String,

    /**
     * The RDF string for [SpdxChecksumAlgorithm].
     */
    val rdf: String,

    /**
     * Whether [SpdxChecksumAlgorithm] is deprecated or not.
     */
    val deprecated: Boolean = false
) {
    MD5("MD5", "checksumAlgorithm_md5"),
    SHA1("SHA1", "checksumAlgorithm_sha1"),
    SHA256("SHA256", "checksumAlgorithm_sha256");

    companion object {
        /**
         * Creates [SpdxChecksumAlgorithm] from string.
         */
        @JsonCreator
        @JvmStatic
        fun fromString(value: String) =
            values().find { value.equals(it.tag, ignoreCase = true) || value.equals(it.rdf, ignoreCase = true) }
    }
}
