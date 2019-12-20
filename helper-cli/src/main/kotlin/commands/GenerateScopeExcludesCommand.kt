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

package com.here.ort.helper.commands

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

import com.here.ort.helper.CommandWithHelp
import com.here.ort.helper.common.minimize
import com.here.ort.helper.common.replaceScopeExcludes
import com.here.ort.helper.common.sortScopeExcludes
import com.here.ort.helper.common.writeAsYaml
import com.here.ort.model.OrtResult
import com.here.ort.model.config.RepositoryConfiguration
import com.here.ort.model.config.ScopeExclude
import com.here.ort.model.config.ScopeExcludeReason
import com.here.ort.model.readValue
import com.here.ort.utils.PARAMETER_ORDER_MANDATORY
import com.here.ort.utils.PARAMETER_ORDER_OPTIONAL

import java.io.File

@Parameters(
    commandNames = ["generate-scope-excludes"],
    commandDescription = "Generate scope excludes based on common default for the package managers. " +
        "The output is written to the given repository configuration file."
)
internal class GenerateScopeExcludesCommand : CommandWithHelp() {
    @Parameter(
        names = ["--ort-result-file"],
        required = true,
        order = PARAMETER_ORDER_MANDATORY,
        description = "The input ORT file from which the rule violations are read."
    )
    private lateinit var ortResultFile: File

    @Parameter(
        names = ["--repository-configuration-file"],
        required = true,
        order = PARAMETER_ORDER_OPTIONAL,
        description = "Override the repository configuration contained in the given input ORT file."
    )
    private lateinit var repositoryConfigurationFile: File

    override fun runCommand(jc: JCommander): Int {
        val ortResult = ortResultFile.readValue<OrtResult>()
        val scopeExcludes = ortResult.generateScopeExcludes()

        repositoryConfigurationFile
            .readValue<RepositoryConfiguration>()
            .replaceScopeExcludes(scopeExcludes)
            .sortScopeExcludes()
            .writeAsYaml(repositoryConfigurationFile)

        return 0
    }
}

private fun OrtResult.generateScopeExcludes(): List<ScopeExclude> {
    val projectScopes = getProjects().flatMap { project ->
        project.scopes.map { it.name }
    }

    return getProjects().flatMap { project ->
        getScopeExcludesForPackageManager(project.id.type)
    }.minimize(projectScopes)
}

private fun getScopeExcludesForPackageManager(packageManagerName: String): List<ScopeExclude> =
    when (packageManagerName) {
        "Bower" -> listOf(
            ScopeExclude(
                pattern = "devDependencies",
                reason = ScopeExcludeReason.DEV_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for development. Not included in released artifacts."
            )
        )
        "Bundler" -> listOf(
            ScopeExclude(
                pattern = "test",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for building the testing. Not included in released artifacts."
            )
        )
        "Cargo" -> listOf(
            ScopeExclude(
                pattern = "build-dependencies",
                reason = ScopeExcludeReason.BUILD_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for building the source code. Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "dev-dependencies",
                reason = ScopeExcludeReason.DEV_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for development. Not included in released artifacts."
            )
        )
        "GoMod" -> listOf(
            ScopeExclude(
                pattern = "all",
                reason = ScopeExcludeReason.BUILD_DEPENDENCY_OF,
                comment = "Scope with dependencies used to build all targets including non-released artifacts like tests."
            )
        )
        "Gradle" -> listOf(
            ScopeExclude(
                pattern = "checkstyle",
                reason = ScopeExcludeReason.BUILD_DEPENDENCY_OF,
                comment = "Scope with dependencies only used to check code styling (testing). Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "detekt",
                reason = ScopeExcludeReason.DEV_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for static code analysis (testing). Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "findbugs",
                reason = ScopeExcludeReason.BUILD_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for static code analysis (testing). Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "jacocoAgent",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for code coverage (testing). Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "jacocoAnt",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for code coverage (testing). Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "kapt.*",
                reason = ScopeExcludeReason.PROVIDED_DEPENDENCY_OF,
                comment = "Scope with dependencies used to process code annotation. Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "lintClassPath",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for code linting (testing). Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "test.*",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for testing. Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = ".*Test.*",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for testing. Not included in released artifacts."
            )
        )
        "Maven" -> listOf(
            ScopeExclude(
                pattern = "provided",
                reason = ScopeExcludeReason.PROVIDED_DEPENDENCY_OF,
                comment = "Scope with dependencies provided by the JDK or container at runtime. Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "test",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for testing. Not included in released artifacts."
            )
        )
        "NPM" -> listOf(
            ScopeExclude(
                pattern = "devDependencies",
                reason = ScopeExcludeReason.DEV_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for development. Not included in released artifacts."
            )
        )
        "PhpComposer" -> listOf(
            ScopeExclude(
                pattern = "require-dev",
                reason = ScopeExcludeReason.DEV_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for development. Not included in released artifacts."
            )
        )
        "SBT" -> listOf(
            ScopeExclude(
                pattern = "provided",
                reason = ScopeExcludeReason.PROVIDED_DEPENDENCY_OF,
                comment = "Scope with dependencies provided by the JDK or container at runtime. Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "test",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for testing. Not included in released artifacts."
            )
        )
        "Stack" -> listOf(
            ScopeExclude(
                pattern = "bench",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for benchmark testing. Not included in released artifacts."
            ),
            ScopeExclude(
                pattern = "test",
                reason = ScopeExcludeReason.TEST_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for testing. Not included in released artifacts."
            )
        )
        "Yarn" -> listOf(
            ScopeExclude(
                pattern = "devDependencies",
                reason = ScopeExcludeReason.DEV_DEPENDENCY_OF,
                comment = "Scope with dependencies only used for development. Not included in released artifacts."
            )
        )
        else -> emptyList()
    }
