/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github.rest.routing

// TODO: Uncomment once https://youtrack.jetbrains.com/issue/KT-28934 is fixed
//class GitHubRoutes(
//    private val username: String,
//    private val password: String
//) : GitHubAPI {
//
//    private val klaxon = Klaxon()
//
//    private val client = HttpClient(Apache) {
//        install(BasicAuth) {
//            username = this@GitHubRoutes.username
//            password = this@GitHubRoutes.password
//        }
//    }
//
//    override val currentUser = username
//
//    override suspend fun getGists() =
//        callAndParse(HttpClient::get, "/users/$username/gists") {
//            parseArray<GitHubGist>(it)?.toImmutableList()
//        }
//
//    override suspend fun getStarredGists() =
//        callAndParse(HttpClient::get, "/gists/starred") {
//            parseArray<GitHubGist>(it)?.toImmutableList()
//        }
//
//    override suspend fun getGist(gistId: String) =
//        callAndParse(HttpClient::get, "/gists/$gistId") {
//            parse<GitHubGist>(it)
//        }
//
//    override suspend fun getGistRevision(gistId: String, revisionSha: String) =
//        callAndParse(HttpClient::get, "/gists/$gistId/$revisionSha") {
//            parse<GitHubGist>(it)
//        }
//
//    override suspend fun createGist(gistCreate: GitHubGistCreate): Either<GitHubError, GitHubGist> {
//        val json = klaxon.toJsonString(gistCreate)
//        return callAndParse(HttpClient::post, "/gists", { body = json }) {
//            parse<GitHubGist>(it)
//        }
//    }
//
//    override suspend fun editGist(gistEdit: GitHubGistEdit): Option<GitHubError> {
//        val json = klaxon.toJsonString(gistEdit)
//        return callAndParse(HttpClient::patch, "/gists/${gistEdit.id}", { body = json }) {
//            Unit
//        }.swap().toOption()
//    }
//
//    override suspend fun getCommits(gistId: String) =
//        callAndParse(HttpClient::get, "/gists/$gistId/commits") {
//            parseArray<GitHubGistCommit>(it)?.toImmutableList()
//        }
//
//    override suspend fun star(gistId: String) =
//        callAndParse(HttpClient::put, "/gists/$gistId/star") {
//            Unit
//        }.swap().toOption()
//
//    override suspend fun unstar(gistId: String) =
//        callAndParse(HttpClient::delete, "/gists/$gistId/star") {
//            Unit
//        }.swap().toOption()
//
//    override suspend fun isStarred(gistId: String) =
//        callAndParse(HttpClient::get, "/gists/$gistId/star") {
//            Unit
//        }.map { false }
//
//    override suspend fun fork(gistId: String) =
//        callAndParse(HttpClient::post, "/gists/$gistId/forks") {
//            parse<GitHubGist>(it)
//        }
//
//    override suspend fun getForks(gistId: String) =
//        callAndParse(HttpClient::get, "/gists/$gistId/forks") {
//            parseArray<GitHubGistFork>(it)?.toImmutableList()
//        }
//
//    override suspend fun delete(gistId: String) =
//        callAndParse(HttpClient::delete, "/gists/$gistId") {
//            Unit
//        }.swap().toOption()
//
//    /**
//     * Runs [call] and [tryParse] with error handling.
//     */
//    private suspend inline fun <reified T> callAndParse(
//        @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
//        method: suspend HttpClient.(String, HttpRequestBuilder.() -> Unit) -> String,
//        route: String,
//        crossinline block: HttpRequestBuilder.() -> Unit = {},
//        parseExpression: Klaxon.(String) -> T?
//    ): Either<GitHubError, T> =
//        Try { call(method, route, block) }
//            .toEither()
//            .mapLeft { failRequest(it) }
//            .flatMap { tryParse(it, parseExpression) }
//
//    /**
//     * Run the [HttpClient] method using the supplied GitHub API v3 [route] and configuration
//     * [block].
//     */
//    private suspend inline fun <reified T> call(
//        @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
//        method: suspend HttpClient.(String, HttpRequestBuilder.() -> Unit) -> T,
//        route: String,
//        crossinline block: HttpRequestBuilder.() -> Unit = {}
//    ): T = client.method("$api$route") {
//        accept(ContentType.parse("application/vnd.github.v3+json"))
//        block()
//    }
//
//    /**
//     * Attempt to parse a non-null result from the [parseInput] using the [parseExpression] and
//     * catch any exceptions encountered.
//     */
//    private inline fun <reified T> tryParse(
//        parseInput: String,
//        parseExpression: Klaxon.(String) -> T?
//    ): Either<GitHubError, T> =
//        Try {
//            klaxon.parseExpression(parseInput)?.right() ?: failParse(parseInput).left()
//        }.toEither()
//            .mapLeft { failParse(it.localizedMessage) }
//            .flatMap { it }
//
//    private fun failRequest(error: Throwable) = GitHubError(
//        """
//        |Could not make request to GitHub:
//        |${error.localizedMessage}
//        """.trimMargin()
//    )
//
//    private fun failParse(json: String) = GitHubError(
//        """
//        |Could not parse GitHubGist from json:
//        |$json
//        """.trimMargin()
//    )
//
//    companion object {
//        const val api = "https://api.github.com"
//
//        @JvmStatic
//        fun main(vararg args: String) {
//            val client = GitHubClient(
//                DefaultGitHubCredentialParser(),
//                Paths.get(System.getProperty("user.home"), ".bowler-git-credentials")
//            )
//            runBlocking {
//                println(
//                    client.getGists().fold({ it.message }, { it.joinToString("\n") })
//                )
////                println(
////                    client.createGist(
////                        GitHubGistPost(
////                            mapOf("file1" to GitHubFilePost("test content")),
////                            "Empty desc",
////                            true
////                        )
////                    ).fold({ it.message }, { it })
////                )
////                println(
////                    client.isStarred("abe9f780cc2e7ca5278f1f465e74313d")
////                )
//            }
//        }
//    }
//}
