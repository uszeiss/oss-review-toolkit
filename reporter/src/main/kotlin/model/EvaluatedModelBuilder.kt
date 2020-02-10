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

import com.here.ort.model.CuratedPackage
import com.here.ort.model.Identifier
import com.here.ort.model.OrtIssue
import com.here.ort.model.OrtResult
import com.here.ort.model.Package
import com.here.ort.model.PackageReference
import com.here.ort.model.Project
import com.here.ort.model.RemoteArtifact
import com.here.ort.model.RuleViolation
import com.here.ort.model.ScanResult
import com.here.ort.model.ScanSummary
import com.here.ort.model.config.IssueResolution
import com.here.ort.model.config.PathExclude
import com.here.ort.model.config.RuleViolationResolution
import com.here.ort.model.config.ScopeExclude
import com.here.ort.model.utils.FindingsMatcher
import com.here.ort.model.yamlMapper
import com.here.ort.reporter.ReporterInput
import com.here.ort.reporter.utils.StatisticsCalculator

/**
 * TODO: docs
 */
class EvaluatedModelBuilder(val input: ReporterInput) {
    private val packages = mutableListOf<EvaluatedPackage>()
    private val dependencyTrees = mutableListOf<DependencyTreeNode>()
    private val scanResults = mutableListOf<EvaluatedScanResult>()
    private val copyrights = mutableListOf<Copyright>()
    private val licenses = mutableListOf<License>()
    private val declaredLicenseStats = mutableMapOf<String, MutableSet<Identifier>>()
    private val detectedLicenseStats = mutableMapOf<String, MutableSet<Identifier>>()
    private val issues = mutableListOf<EvaluatedOrtIssue>()
    private val issueResolutions = mutableListOf<IssueResolution>()
    private val pathExcludes = mutableListOf<PathExclude>()
    private val scopeExcludes = mutableListOf<ScopeExclude>()
    private val violations = mutableListOf<EvaluatedRuleViolation>()
    private val violationResolutions = mutableListOf<RuleViolationResolution>()

    private val findingsMatcher = FindingsMatcher()

    fun build(): EvaluatedModel {
        input.ortResult.analyzer?.result?.projects?.forEach { project ->
            val pkg = packages.find { it.id == project.id }!!
            addDependencyTree(project, pkg)
        }

        return EvaluatedModel(
            packages = packages,
            dependencyTrees = dependencyTrees,
            scanResults = scanResults,
            copyrights = copyrights.toSet(),
            licenses = licenses.toSet(),
            declaredLicenseStats = declaredLicenseStats.mapValuesTo(sortedMapOf()) { it.value.size },
            detectedLicenseStats = detectedLicenseStats.mapValuesTo(sortedMapOf()) { it.value.size },
            issues = issues,
            issueResolutions = issueResolutions,
            violations = violations,
            violationResolutions = violationResolutions,
            pathExcludes = pathExcludes,
            scopeExcludes = scopeExcludes,
            statistics = StatisticsCalculator().getStatistics(input.ortResult, input.resolutionProvider),
            repositoryConfiguration = yamlMapper.writeValueAsString(input.ortResult.repository.config),
            customData = input.ortResult.data
        )
    }

    fun addProject(project: Project) {
        val scanResults = mutableListOf<EvaluatedScanResult>()
        val detectedLicenses = mutableSetOf<License>()
        val findings = mutableListOf<EvaluatedFinding>()
        val issues = mutableListOf<EvaluatedOrtIssue>()

        val applicablePathExcludes = input.ortResult.getExcludes().findPathExcludes(project, input.ortResult)
        val evaluatedPathExcludes = pathExcludes.addIfRequired(applicablePathExcludes)

        val evaluatedPackage = EvaluatedPackage(
            id = project.id,
            isProject = true,
            definitionFilePath = project.definitionFilePath,
            purl = project.id.toPurl(), // TODO: Add PURL to Project class.
            declaredLicenses = project.declaredLicenses,
            declaredLicensesProcessed = project.declaredLicensesProcessed,
            detectedLicenses = detectedLicenses,
            concludedLicense = null,
            description = "",
            homepageUrl = project.homepageUrl,
            binaryArtifact = RemoteArtifact.EMPTY, // Should be nullable?
            sourceArtifact = RemoteArtifact.EMPTY, // Should be nullable?
            vcs = project.vcs,
            vcsProcessed = project.vcsProcessed,
            curations = emptyList(),
            paths = mutableListOf(),
            levels = sortedSetOf(0),
            scanResults = scanResults,
            findings = findings,
            isExcluded = applicablePathExcludes.isNotEmpty(),
            pathExcludes = evaluatedPathExcludes,
            scopeExcludes = emptyList(),
            issues = issues
        )

        val actualPackage = packages.addIfRequired(evaluatedPackage)

        project.declaredLicensesProcessed.allLicenses.forEach { license ->
            val actualLicense = licenses.addIfRequired(License(license))
            declaredLicenseStats.count(actualLicense.id, actualPackage.id)
        }

        issues += addAnalyzerIssues(project.id, actualPackage)

        input.ortResult.getScanResultsForId(project.id).mapTo(scanResults) { result ->
            convertScanResult(result, findings, actualPackage)
        }

        findings.filter { it.type == EvaluatedFindingType.LICENSE }.mapNotNullTo(detectedLicenses) { it.license }
    }

    fun addPackage(curatedPkg: CuratedPackage) {
        val pkg = curatedPkg.pkg

        val scanResults = mutableListOf<EvaluatedScanResult>()
        val detectedLicenses = mutableSetOf<License>()
        val findings = mutableListOf<EvaluatedFinding>()
        val issues = mutableListOf<EvaluatedOrtIssue>()

        val isExcluded = input.ortResult.isPackageExcluded(curatedPkg.pkg.id)
        val (applicablePathExcludes, applicableScopeExcludes) = if (isExcluded) {
            Pair(input.ortResult.findPathExcludes(pkg), input.ortResult.findScopeExcludes(pkg))
        } else {
            Pair(emptySet(), emptySet())
        }

        val evaluatedPathExcludes = pathExcludes.addIfRequired(applicablePathExcludes)
        val evaluatedScopeExcludes = scopeExcludes.addIfRequired(applicableScopeExcludes)

        val evaluatedPackage = EvaluatedPackage(
            id = pkg.id,
            isProject = false,
            definitionFilePath = "",
            purl = pkg.purl,
            declaredLicenses = pkg.declaredLicenses,
            declaredLicensesProcessed = pkg.declaredLicensesProcessed,
            detectedLicenses = detectedLicenses,
            concludedLicense = pkg.concludedLicense,
            description = pkg.description,
            homepageUrl = pkg.homepageUrl,
            binaryArtifact = pkg.binaryArtifact,
            sourceArtifact = pkg.sourceArtifact,
            vcs = pkg.vcs,
            vcsProcessed = pkg.vcsProcessed,
            curations = curatedPkg.curations,
            paths = mutableListOf(),
            levels = sortedSetOf(),
            scanResults = scanResults,
            findings = findings,
            isExcluded = isExcluded,
            pathExcludes = evaluatedPathExcludes,
            scopeExcludes = evaluatedScopeExcludes,
            issues = issues
        )

        val actualPackage = packages.addIfRequired(evaluatedPackage)

        pkg.declaredLicensesProcessed.allLicenses.forEach { license ->
            val actualLicense = licenses.addIfRequired(License(license))
            declaredLicenseStats.count(actualLicense.id, actualPackage.id)
        }

        issues += addAnalyzerIssues(pkg.id, actualPackage)

        input.ortResult.getScanResultsForId(pkg.id).mapTo(scanResults) { result ->
            convertScanResult(result, findings, actualPackage)
        }

        findings.filter { it.type == EvaluatedFindingType.LICENSE }.mapNotNullTo(detectedLicenses) { it.license }
    }

    private fun addAnalyzerIssues(id: Identifier, pkg: EvaluatedPackage): List<EvaluatedOrtIssue> {
        input.ortResult.analyzer?.result?.issues?.get(id)?.let { analyzerIssues ->
            return addIssues(analyzerIssues, EvaluatedOrtIssueType.ANALYZER, pkg, null, null)
        }
        return emptyList()
    }

    fun addRuleViolation(ruleViolation: RuleViolation) {
        val resolutions = addResolutions(ruleViolation)
        val pkg = packages.find { it.id == ruleViolation.pkg }!!

        val evaluatedViolation = EvaluatedRuleViolation(
            rule = ruleViolation.rule,
            pkg = pkg,
            license = ruleViolation.license,
            licenseSource = ruleViolation.licenseSource,
            severity = ruleViolation.severity,
            message = ruleViolation.message,
            howToFix = ruleViolation.howToFix,
            resolutions = resolutions
        )

        violations += evaluatedViolation
    }

    private fun convertScanResult(
        result: ScanResult,
        findings: MutableList<EvaluatedFinding>,
        pkg: EvaluatedPackage
    ): EvaluatedScanResult {
        val issues = mutableListOf<EvaluatedOrtIssue>()

        val evaluatedScanResult = EvaluatedScanResult(
            provenance = result.provenance,
            scanner = result.scanner,
            startTime = result.summary.startTime,
            endTime = result.summary.endTime,
            fileCount = result.summary.fileCount,
            packageVerificationCode = result.summary.packageVerificationCode,
            issues = issues
        )

        val actualScanResult = scanResults.addIfRequired(evaluatedScanResult)

        issues += addIssues(
            result.summary.issues,
            EvaluatedOrtIssueType.SCANNER,
            pkg,
            actualScanResult,
            null
        )

        addLicensesAndCopyrights(result.summary, actualScanResult, pkg, findings)

        return actualScanResult
    }

    private fun addDependencyTree(project: Project, pkg: EvaluatedPackage) {
        fun PackageReference.toEvaluatedTreeNode(scope: String, path: List<Identifier>): DependencyTreeNode {
            val packageIndex = packages.indexOfFirst { it.id == id }
            val issues = mutableListOf<EvaluatedOrtIssue>()
            if (packageIndex >= 0) {
                val packagePath = EvaluatedPackagePath(
                    project = pkg.id,
                    scope = scope,
                    packages = path
                )

                packages[packageIndex].paths += packagePath
                packages[packageIndex].levels += path.size

                issues += addIssues(this.issues, EvaluatedOrtIssueType.ANALYZER, pkg, null, packagePath)
            }

            return DependencyTreeNode(
                title = id.toCoordinates(),
                pkg = pkg,
                children = dependencies.map { it.toEvaluatedTreeNode(scope, path + pkg.id) },
                pathExcludes = emptyList(),
                scopeExcludes = emptyList(),
                issues = issues
            )
        }

        val scopeTrees = project.scopes.map { scope ->
            val subTrees = scope.dependencies.map { it.toEvaluatedTreeNode(scope.name, listOf(pkg.id)) }

            val applicableScopeExcludes = input.ortResult.getExcludes().findScopeExcludes(scope)
            val evaluatedScopeExcludes = scopeExcludes.addIfRequired(applicableScopeExcludes)

            DependencyTreeNode(
                title = scope.name,
                pkg = null,
                children = subTrees,
                pathExcludes = emptyList(),
                scopeExcludes = evaluatedScopeExcludes,
                issues = emptyList()
            )
        }

        val tree = DependencyTreeNode(
            title = project.id.toCoordinates(),
            pkg = pkg,
            children = scopeTrees,
            pathExcludes = pkg.pathExcludes,
            scopeExcludes = emptyList(),
            issues = emptyList()
        )

        dependencyTrees += tree
    }

    private fun addIssues(
        issues: List<OrtIssue>,
        type: EvaluatedOrtIssueType,
        pkg: EvaluatedPackage, // TODO: Replace with identifier
        scanResult: EvaluatedScanResult?,
        path: EvaluatedPackagePath?
    ): List<EvaluatedOrtIssue> {
        val evaluatedIssues = issues.map { issue ->
            val resolutions = addResolutions(issue)

            EvaluatedOrtIssue(
                timestamp = issue.timestamp,
                type = type,
                source = issue.source,
                message = issue.message,
                severity = issue.severity,
                resolutions = resolutions,
                pkg = pkg,
                scanResult = scanResult,
                path = path
            )
        }

        return this.issues.addIfRequired(evaluatedIssues)
    }

    private fun addResolutions(issue: OrtIssue): List<IssueResolution> {
        val matchingResolutions = input.resolutionProvider.getIssueResolutionsFor(issue)

        return issueResolutions.addIfRequired(matchingResolutions)
    }

    private fun addResolutions(ruleViolation: RuleViolation): List<RuleViolationResolution> {
        val matchingResolutions = input.resolutionProvider.getRuleViolationResolutionsFor(ruleViolation)

        return violationResolutions.addIfRequired(matchingResolutions)
    }

    private fun addLicensesAndCopyrights(
        summary: ScanSummary,
        scanResult: EvaluatedScanResult,
        pkg: EvaluatedPackage,
        findings: MutableList<EvaluatedFinding>
    ) {
        val matchedFindings = findingsMatcher.match(
            summary.licenseFindings,
            summary.copyrightFindings
        )

        matchedFindings.forEach { licenseFindings ->

            licenseFindings.copyrights.forEach { copyrightFinding ->
                val actualCopyright = copyrights.addIfRequired(Copyright(copyrightFinding.statement))

                copyrightFinding.locations.forEach { location ->
                    findings += EvaluatedFinding(
                        type = EvaluatedFindingType.COPYRIGHT,
                        license = null,
                        copyright = actualCopyright,
                        path = location.path,
                        startLine = location.startLine,
                        endLine = location.endLine,
                        scanResult = scanResult
                    )
                }
            }

            val actualLicense = licenses.addIfRequired(License(licenseFindings.license))
            detectedLicenseStats.count(actualLicense.id, pkg.id)

            licenseFindings.locations.forEach { location ->
                findings += EvaluatedFinding(
                    type = EvaluatedFindingType.LICENSE,
                    license = actualLicense,
                    copyright = null,
                    path = location.path,
                    startLine = location.startLine,
                    endLine = location.endLine,
                    scanResult = scanResult
                )
            }
        }
    }

    private fun <T> MutableList<T>.addIfRequired(value: T): T {
        val existingValue = find { it == value }

        return if (existingValue != null) {
            existingValue
        } else {
            add(value)
            value
        }
    }

    private fun <T> MutableList<T>.addIfRequired(values: Collection<T>): List<T> {
        val result = mutableListOf<T>()

        values.forEach { value ->
            val existingValue = find { it == value }
            if (existingValue != null) {
                result += existingValue
            } else {
                add(value)
                result += value
            }
        }

        return result.distinct()
    }

    private fun MutableMap<String, MutableSet<Identifier>>.count(key: String, value: Identifier) {
        this.getOrPut(key) { mutableSetOf() } += value
    }

    // TODO: Move this function to OrtResult. Consider changing PackageEntry to contain the excludes instead of only isExcluded.
    private fun OrtResult.findPathExcludes(pkg: Package): Set<PathExclude> {
        val excludes = mutableSetOf<PathExclude>()

        getProjects().forEach { project ->
            if (project.dependsOn(pkg.id)) {
                excludes += getExcludes().findPathExcludes(project, this)
            }
        }

        return excludes
    }

    // TODO: Move this function to OrtResult.
    private fun OrtResult.findScopeExcludes(pkg: Package): Set<ScopeExclude> {
        val excludes = mutableSetOf<ScopeExclude>()

        getProjects().forEach { project ->
            project.scopes.forEach { scope ->
                if (scope.contains(pkg.id)) {
                    excludes += getExcludes().findScopeExcludes(scope)
                }
            }
        }

        return excludes
    }

    // TODO: Move to Project.
    private fun Project.dependsOn(id: Identifier): Boolean = scopes.any { it.contains(id) }
}
