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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * A class to denote the type of [SpdxRelationship].
 */
@JacksonXmlRootElement(
    localName = "spdx:relationshipType"
)
enum class SpdxRelationshipType {
    /**
     * Use when SPDX element A is an ancestor of SPDX element B (same lineage but pre-dates).
     */
    ANCESTOR_OF,

    /**
     * Use when SPDX element A amends the SPDX information in SPDX element B.
     */
    AMENDS,

    /**
     * Use when SPDX element A is used to build SPDX element B.
     */
    BUILD_TOOL_OF,

    /**
     * Use when SPDX element A contains SPDX element B.
     * For example, a package A includes a file B.
     */
    CONTAINS,

    /**
     * Use when SPDX element A is contained by SPDX element B.
     * For example, a file A is contained in a package B.
     */
    CONTAINED_BY,

    /**
     * Use when SPDX element A is an exact copy of SPDX element B.
     */
    COPY_OF,

    /**
     * Is to be used when SPDXRef-A is a data file used in SPDXRef-B.
     */
    DATA_FILE_OF,

    /**
     * Use when SPDX element is a descendant of SPDX element B (same lineage but post-dates).
     */
    DESCENDANT_OF,

    /**
     * Use when SPDX document A describes to SPDX element B.
     */
    DESCRIBES,

    /**
     * Use when SPDX file A is described by SPDX document B.
     */
    DESCRIBED_BY,

    /**
     * Use when distributing SPDX element A requires also distributing SPDX element B.
     */
    DISTRIBUTION_ARTIFACT,

    /**
     * Use when SPDX element A dynamically links to SPDX element B.
     */
    DOCUMENTATION_OF,

    /**
     * Use when SPDX element A dynamically links to SPDX element B.
     */
    DYNAMIC_LINK,

    /**
     * Use when SPDX file A was expanded / extracted from SPDX file B.
     */
    EXPANDED_FROM_ARCHIVE,

    /**
     * Use when SPDX file A has been added SPDX package B.
     */
    FILE_ADDED,

    /**
     * Use when SPDX package A from which SPDX element B was removed.
     */
    FILE_DELETED,

    /**
     *  Use when SPDX element A is a file which is a modified version of  SPDX element B.
     */
    FILE_MODIFIED,

    /**
     * Use when SPDX element A is generated from SPDX element B.
     */
    GENERATED_FROM,

    /**
     * Use when SPDX element A generates SPDX element B.
     */
    GENERATES,

    /**
     * Use when SPDX element A has as a prerequisite SPDX element B.
     */
    HAS_PREREQUISITE,

    /**
     * Use when SPDX element A is a metafile of SPDX element B.
     */
    METAFILE_OF,

    /**
     * Use when SPDX element A is an optional component of SPDX element B.
     */
    OPTIONAL_COMPONENT_OF,

    /**
     * Use for relationships which have not been defined in the formal SPDX specification.
     * A description of the relationship should be included in the Relationship comments field.
     */
    OTHER,

    /**
     * Use when SPDX element A is a package that is part of SPDX element B.
     */
    PACKAGE_OF,

    /**
     * Use when SPDX element A is 'patchfile' thay was applied and produced SPDX element B.
     */
    PATCH_APPLIED,

    /**
     * Use when SPDX element A is a 'patchfile' that is designed to patch (apply modifications to) the SPDX element B.
     */
    PATCH_FOR,

    /**
     * Use when SPDX element A is a prerequisite for SPDX element B.
     */
    PREREQUISITE_FOR,

    /**
     * Use when SPDX element A statically links to SPDX element B.
     */
    STATIC_LINK,

    /**
     * Use when SPDX element A is a test case used in testing SPDX element B.
     */
    TEST_CASE_OF,

    /**
     * Use when SPDX element A is a variant of SPDX element B, but it is not clear which came first.
     * For example, if the contents of two files differs by some edit, but there is no way to tell which came first
     * due to no reliable date information, then one file is a variant of the other file.
     */
    VARIANT_OF;
}
