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
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * Identifier based on fingerprinting actual files within [SpdxPackage].
 */
@JacksonXmlRootElement(
    namespace = "spdx",
    localName = "spdx:PackageVerificationCode"
)
@JsonRootName(value = "PackageVerificationCode")
@JsonSerialize(using = SpdxPackageVerificationCodeSerializer::class)
data class SpdxPackageVerificationCode(
    /**
     * Value for [SpdxPackageVerificationCode].
     * Cardinality: mandatory, one.
     */
    @JacksonXmlProperty(
        isAttribute = false,
        namespace = "spdx",
        localName = "packageVerificationCodeExcludedFile"
    )
    val packageVerificationCodeValue: String,

    /**
     * Whether a file .
     * Cardinality: optional, one.
     */
    @JacksonXmlProperty(
      isAttribute = true,
      namespace = "spdx",
      localName = "packageVerificationCodeExcludedFile"
    )
    val packageVerificationCodeExcludedFile: String? = ""

    ) : Comparable<SpdxPackageVerificationCode> {
    companion object {
        /**
         * A constant for a [SpdxPackageVerificationCode] where all properties are empty.
         */
        @JvmField
        val EMPTY = SpdxPackageVerificationCode(
            packageVerificationCodeValue = "",
            packageVerificationCodeExcludedFile = ""
        )
    }

    override fun compareTo(other: SpdxPackageVerificationCode) =
        compareValuesBy(
            this,
            other,
            compareBy(SpdxPackageVerificationCode::packageVerificationCodeValue)
                .thenBy(SpdxPackageVerificationCode::packageVerificationCodeExcludedFile)
        ) { it }
}

class SpdxPackageVerificationCodeSerializer : StdSerializer<SpdxPackageVerificationCode>(SpdxPackageVerificationCode::class.java) {
    override fun serialize(value: SpdxPackageVerificationCode, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()

        if (value.packageVerificationCodeExcludedFile.isNullOrEmpty()) {
            gen.writeObjectField(
                "PackageVerificationCode",
                value.packageVerificationCodeValue
            )
        } else {
            gen.writeObjectField(
                "PackageVerificationCode",
                "${value.packageVerificationCodeValue} (excludes: ${value.packageVerificationCodeExcludedFile})"
            )
        }

        gen.writeEndObject()
    }
}
