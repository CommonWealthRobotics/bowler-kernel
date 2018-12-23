/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.right
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.createMockGist
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.model.createMockGistFile
import com.neuronrobotics.bowlerkernel.gitfs.github.rest.routing.GitHubAPI
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTimeout
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.Duration
import kotlin.concurrent.thread

internal class GistScriptTest {

    @Test
    fun `test run script with a hello world script`() {
        val gistId = "gistId"
        val filename = "theFile.groovy"
        val language = "groovy"
        val scriptContent = """ return "Hello, World!" """
        val mockGitHubAPI = mock<GitHubAPI> {
            on { runBlocking { getGist(gistId) } } doReturn createMockGist(
                gistId,
                mapOf(createMockGistFile(filename, scriptContent))
            ).right()
        }

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn ScriptLanguage.Groovy
        }

        val script = GistScript.Factory(mockGitHubAPI, mockScriptLanguageParser)
            .createGistScript(gistId, filename)

        val result = script.runScript(emptyImmutableList())
        assertAll(
            { assertTrue(result.isRight()) },
            { assertEquals("Hello, World!", result.fold({ null }, { it as? String })) }
        )
    }

    @Test
    fun `test run script with a parse error`() {
        val gistId = "gistId"
        val filename = "theFile.groovy"
        val language = "groovy"
        val scriptContent = """ return "Hello, World!" """
        val mockGitHubAPI = mock<GitHubAPI> {
            on { runBlocking { getGist(gistId) } } doReturn createMockGist(
                gistId,
                mapOf(createMockGistFile(filename, scriptContent))
            ).right()
        }

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn ScriptLanguage.ParseError("")
        }

        val script = GistScript.Factory(mockGitHubAPI, mockScriptLanguageParser)
            .createGistScript(gistId, filename)

        val result = script.runScript(emptyImmutableList())
        assertTrue(result.isLeft())
    }

    @Test
    fun `test interrupting a script`() {
        val gistId = "gistId"
        val filename = "theFile.groovy"
        val language = "groovy"
        val scriptContent = """
            |try {
            |   Thread.sleep(10000)
            |} catch (InterruptedException ignored) {
            |   return
            |}
            """.trimMargin()

        val mockGitHubAPI = mock<GitHubAPI> {
            on { runBlocking { getGist(gistId) } } doReturn createMockGist(
                gistId,
                mapOf(createMockGistFile(filename, scriptContent))
            ).right()
        }

        val mockScriptLanguageParser = mock<ScriptLanguageParser> {
            on { parse(language) } doReturn ScriptLanguage.Groovy
        }

        val script = GistScript.Factory(mockGitHubAPI, mockScriptLanguageParser)
            .createGistScript(gistId, filename)

        assertTimeout(Duration.ofSeconds(2)) {
            thread { script.runScript(emptyImmutableList()) }
            runBlocking { delay(1000) }
            script.stopScript()
        }
    }
}
