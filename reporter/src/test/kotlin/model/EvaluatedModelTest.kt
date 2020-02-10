/*
 * Copyright (C) 2017-2020 HERE Europe B.V.
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

package com.here.ort.reporter.reporters

import com.here.ort.model.OrtResult
import com.here.ort.model.config.CopyrightGarbage
import com.here.ort.model.config.OrtConfiguration
import com.here.ort.model.config.Resolutions
import com.here.ort.model.licenses.LicenseConfiguration
import com.here.ort.model.readValue
import com.here.ort.reporter.DefaultLicenseTextProvider
import com.here.ort.reporter.DefaultResolutionProvider
import com.here.ort.reporter.ReporterInput
import com.here.ort.reporter.model.EvaluatedModel
import com.here.ort.utils.expandTilde

import io.kotlintest.specs.WordSpec

import java.io.File

class EvaluatedModelTest : WordSpec({
    "create()" should {
        "create the expected model" {
            val ortResult = File("~/evaluator/mime-types/evaluation-result.json").expandTilde().readValue<OrtResult>()

            val resolutionProvider = DefaultResolutionProvider()
            resolutionProvider.add(ortResult.getResolutions())
            File("~/git/oss/configuration/resolutions.yml").expandTilde().readValue<Resolutions>()
                .let { resolutionProvider.add(it) }

            val licenseConfiguration =
                File("~/git/oss/configuration/licenses.yml").expandTilde().readValue<LicenseConfiguration>()

            val input = ReporterInput(
                ortResult = ortResult,
                ortConfig = OrtConfiguration(),
                resolutionProvider = resolutionProvider,
                licenseTextProvider = DefaultLicenseTextProvider(),
                copyrightGarbage = CopyrightGarbage(),
                licenseConfiguration = licenseConfiguration
            )

            val evaluatedModel = EvaluatedModel.create(input)

            val json = evaluatedModel.toJson()
            println(json)

            val outputFile = File("~/web-app-model.json").expandTilde()
            outputFile.writeText(json)
        }
    }
})
