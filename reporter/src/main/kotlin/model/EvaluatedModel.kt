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

package com.here.ort.reporter.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.ObjectIdResolver
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import com.here.ort.model.CustomData
import com.here.ort.model.Identifier
import com.here.ort.model.LicenseSource
import com.here.ort.model.OrtIssue
import com.here.ort.model.OrtResult
import com.here.ort.model.PROPERTY_NAMING_STRATEGY
import com.here.ort.model.PackageCurationResult
import com.here.ort.model.Provenance
import com.here.ort.model.RemoteArtifact
import com.here.ort.model.RuleViolation
import com.here.ort.model.ScannerDetails
import com.here.ort.model.Severity
import com.here.ort.model.VcsInfo
import com.here.ort.model.config.IssueResolution
import com.here.ort.model.config.LicenseFindingCuration
import com.here.ort.model.config.PathExclude
import com.here.ort.model.config.RepositoryConfiguration
import com.here.ort.model.config.RuleViolationResolution
import com.here.ort.model.config.ScopeExclude
import com.here.ort.reporter.Reporter
import com.here.ort.reporter.ReporterInput
import com.here.ort.reporter.reporters.WebAppReporter
import com.here.ort.reporter.utils.IntIdModule
import com.here.ort.spdx.SpdxExpression
import com.here.ort.utils.DeclaredLicenseProcessor
import com.here.ort.utils.ProcessedDeclaredLicense

import java.io.File
import java.time.Instant
import java.util.SortedMap
import java.util.SortedSet

/**
 * The [EvaluatedModel] represents the outcome of the evaluation of a [ReporterInput]. This means that all additional
 * information contained in the [ReporterInput] are applied to the [OrtResult]:
 *
 * * [PathExclude]s and [ScopeExclude]s from the [RepositoryConfiguration] are applied.
 * * [IssueResolution]s from the [ReporterInput.resolutionProvider] are matched against all [OrtIssue]s contained in the
 *   result.
 * * [RuleViolationResolution]s from the [ReporterInput.resolutionProvider] are matched against all [RuleViolation]s.
 *
 * The current implementation is missing these features:
 *
 * * [LicenseFindingCuration]s are not yet applied to the model.
 *
 * The model also contain useful containers to easily access some content of the [OrtResult], for example a list of
 * all [OrtIssue]s in their [evaluated form][EvaluatedOrtIssue] which contains back-references to the sources of the
 * issues.
 *
 * For JSON (de-)serialization the model provides the helper functions [fromFile], [fromJson], and [toJson]. These use
 * a special [JsonMapper] that de-duplicates objects in the result. For this it uses Jackson's [JsonIdentityInfo] to
 * automatically generate [Int] IDs for the objects. All object for which the model contains containers, like [issues]
 * or [packages] are serialized only once in those containers. All other references to those objects are replaced by the
 * [Int] IDs. This is required because the model contains cyclic dependencies between objects which would otherwise
 * cause stack overflows during serialization, and it also reduces the size of the result file.
 *
 * Use cases for the [EvaluatedModel] are:
 *
 * * Input for [Reporter] implementations to not have to repeatedly implement the application of excludes, resolutions,
 *   and so on.
 * * Input for external tools, so that they do not have to re-implement the logic for evaluating the model.
 * * Input for the [WebAppReporter], so that it does not have to evaluate the model at runtime.
 *
 * Important notes for working with this model:
 *
 * * The model uses Kotlin data classes with cyclic dependencies, therefore the [hashCode] and [toString] of affected
 *   classes cannot be used, because they would create stack overflows.
 * * When modifying the model make sure that the objects are serialized at the right place. By default Jackson
 *   serializes an Object with [ObjectIdInfo] the first time the serializer sees the object. If this is not desired
 *   because the object shall be serialized as the generated ID, the [JsonIdentityReference] annotation can be used to
 *   enforce this. For example, the list of [EvaluatedOrtIssue]s is serialized before the list of [EvaluatedPackage]s.
 *   Therefore [EvaluatedOrtIssue.pkg] is annotated with [JsonIdentityReference].
 * * There is no easy way to deserialize cyclic dependencies between objects. Jackson allows to implement a custom
 *   [ObjectIdResolver] for this use case, but it is mainly intended to be used to request objects from an external
 *   like a database. To solve this issue cyclic references need to be excluded from deserialization and manually set
 *   after deserialization, like it is done for
 */
data class EvaluatedModel(
    val pathExcludes: List<PathExclude>,
    val scopeExcludes: List<ScopeExclude>,
    val issueResolutions: List<IssueResolution>,
    val issues: List<EvaluatedOrtIssue>,
    val copyrights: Set<Copyright>,
    val licenses: Set<License>,
    val scanResults: List<EvaluatedScanResult>,
    val packages: List<EvaluatedPackage>,
    val dependencyTrees: List<DependencyTreeNode>,
    val violationResolutions: List<RuleViolationResolution>,
    val violations: List<EvaluatedRuleViolation>,
    val declaredLicenseStats: SortedMap<String, Int>,
    val detectedLicenseStats: SortedMap<String, Int>,
    val statistics: Statistics,
    val repositoryConfiguration: String,
    val customData: CustomData
) {
    companion object {
        private val INT_ID_TYPES = listOf(
            Copyright::class.java,
            EvaluatedOrtIssue::class.java,
            EvaluatedPackage::class.java,
            EvaluatedRuleViolation::class.java,
            EvaluatedScanResult::class.java,
            IssueResolution::class.java,
            License::class.java,
            PathExclude::class.java,
            RuleViolationResolution::class.java,
            ScopeExclude::class.java
        )

        private val MAPPER = JsonMapper().apply {
            registerKotlinModule()

            registerModule(JavaTimeModule())
            registerModule(IntIdModule(INT_ID_TYPES))

            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            propertyNamingStrategy = PROPERTY_NAMING_STRATEGY
        }

        fun create(input: ReporterInput): EvaluatedModel {
            val builder = EvaluatedModelBuilder(input)

            input.ortResult.analyzer?.result?.projects?.forEach { project ->
                builder.addProject(project)
            }

            input.ortResult.analyzer?.result?.packages?.forEach { curatedPkg ->
                builder.addPackage(curatedPkg)
            }

            input.ortResult.evaluator?.violations?.forEach { ruleViolation ->
                builder.addRuleViolation(ruleViolation)
            }

            return builder.build()
        }

        fun fromFile(file: File): EvaluatedModel = MAPPER.readValue<EvaluatedModel>(file).recreateReferences()

        fun fromJson(json: String): EvaluatedModel =
            MAPPER.readValue(json, EvaluatedModel::class.java).recreateReferences()

        private fun EvaluatedModel.recreateReferences(): EvaluatedModel {
            packages.forEach { pkg ->
                pkg.issues.forEach { it.pkg = pkg }
            }

            scanResults.forEach { scanResult ->
                scanResult.issues.forEach { it.scanResult = scanResult }
            }

            return this
        }
    }

    fun toJson(): String = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this)
}

data class EvaluatedPackage(
    val id: Identifier,
    val isProject: Boolean,
    val definitionFilePath: String,
    val purl: String = id.toPurl(),
    val declaredLicenses: SortedSet<String>,
    val declaredLicensesProcessed: ProcessedDeclaredLicense = DeclaredLicenseProcessor.process(declaredLicenses),
    val detectedLicenses: Set<License>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val concludedLicense: SpdxExpression? = null,
    val description: String,
    val homepageUrl: String,
    val binaryArtifact: RemoteArtifact,
    val sourceArtifact: RemoteArtifact,
    val vcs: VcsInfo,
    val vcsProcessed: VcsInfo = vcs.normalize(),
    val curations: List<PackageCurationResult>,
    val paths: MutableList<EvaluatedPackagePath>,
    val levels: SortedSet<Int>,
    val scanResults: List<EvaluatedScanResult>,
    val findings: List<EvaluatedFinding>,
    val isExcluded: Boolean,
    val pathExcludes: List<PathExclude>,
    val scopeExcludes: List<ScopeExclude>,
    val issues: List<EvaluatedOrtIssue>
)

data class EvaluatedPackagePath(
    val project: Identifier,
    val scope: String,
    val packages: List<Identifier>
)

data class EvaluatedScanResult(
    val provenance: Provenance,
    val scanner: ScannerDetails,
    val startTime: Instant,
    val endTime: Instant,
    val fileCount: Int,
    val packageVerificationCode: String,
    val issues: List<EvaluatedOrtIssue>
)

enum class EvaluatedFindingType {
    COPYRIGHT, LICENSE
}

data class EvaluatedFinding(
    val type: EvaluatedFindingType,
    val license: License?,
    val copyright: Copyright?,
    val path: String,
    val startLine: Int,
    val endLine: Int,
    val scanResult: EvaluatedScanResult
)

enum class EvaluatedOrtIssueType {
    ANALYZER, SCANNER
}

data class Copyright(
    val statement: String
)

data class License(
    val id: String
)

data class EvaluatedOrtIssue(
    val timestamp: Instant,
    val type: EvaluatedOrtIssueType,
    val source: String,
    val message: String,
    val severity: Severity = Severity.ERROR,
    val resolutions: List<IssueResolution>,
    @JsonIdentityReference(alwaysAsId = true)
    @get:JsonIgnore
    var pkg: EvaluatedPackage?,
    @JsonIdentityReference(alwaysAsId = true)
    @get:JsonIgnore
    var scanResult: EvaluatedScanResult?, // Only for scanner issues.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val path: EvaluatedPackagePath? = null // Only for issues in package references.
)

data class EvaluatedRuleViolation(
    val rule: String,
    @JsonIdentityReference(alwaysAsId = true)
    val pkg: EvaluatedPackage,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val license: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val licenseSource: LicenseSource? = null,
    val severity: Severity,
    val message: String,
    val howToFix: String,
    val resolutions: List<RuleViolationResolution>
)

data class DependencyTreeNode(
    val title: String,
    val key: Int = nextKey(),
    val pkg: EvaluatedPackage?,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val pathExcludes: List<PathExclude> = emptyList(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val scopeExcludes: List<ScopeExclude> = emptyList(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val issues: List<EvaluatedOrtIssue> = emptyList(),
    val children: List<DependencyTreeNode>
) {
    companion object {
        private var lastKey = -1
        private fun nextKey() = ++lastKey
    }
}
