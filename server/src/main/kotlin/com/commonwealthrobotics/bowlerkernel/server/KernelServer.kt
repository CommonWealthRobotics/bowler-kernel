/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.commonwealthrobotics.bowlerkernel.server

import com.commonwealthrobotics.bowlerkernel.di.GITHUB_CACHE_DIRECTORY_KOIN_NAME
import com.commonwealthrobotics.bowlerkernel.gitfs.DefaultDependencyResolver
import com.commonwealthrobotics.bowlerkernel.gitfs.DependencyResolver
import com.commonwealthrobotics.bowlerkernel.scripthost.ScriptHost
import com.commonwealthrobotics.bowlerkernel.scripting.DefaultScriptLoader
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import com.commonwealthrobotics.bowlerkernel.util.getFullPathToGitHubCacheDirectory
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.nio.file.Path
import java.util.concurrent.Executors

class KernelServer {

    private var server: Server? = null
    internal lateinit var koinComponent: KoinComponent

    /**
     * The port number the server is listening on. `-1` if the server is not running.
     */
    val port: Int
        get() = try {
            server?.port ?: -1
        } catch (ex: IllegalStateException) {
            -1
        }

    /**
     * Starts the server if it is not running. Does nothing if the server is running.
     *
     * @param gitHubCacheDirectory The path under which the kernel's GitFS should cache GitHub assets.
     */
    fun ensureStarted(gitHubCacheDirectory: Path = getFullPathToGitHubCacheDirectory()) {
        synchronized(this) {
            if (server != null) {
                // Do nothing if the server is already started
                return
            }

            koinComponent = object : KoinComponent {
                private val koinApp = koinApplication {
                    modules(
                        module {
                            single(named(GITHUB_CACHE_DIRECTORY_KOIN_NAME)) { gitHubCacheDirectory }
                            factory<DependencyResolver> { DefaultDependencyResolver(get()) }
                            factory<ScriptLoader> { DefaultScriptLoader(get()) }
                        }
                    )
                }

                override fun getKoin() = koinApp.koin
            }

            // TODO: Support setting the port number via a cmdline option
            server = NettyServerBuilder.forPort(0).apply {
                // TODO: Support SSL
                addService(ScriptHost(CoroutineScope(Dispatchers.Default), koinComponent))
            }.build().also {
                it.start()
                logger.info { "Server running on port ${it.port}" }
            }
        }
    }

    /**
     * Stops the server if it is running. Does nothing if the server is not running.
     */
    fun ensureStopped() {
        synchronized(this) {
            server?.shutdown()?.awaitTermination()
            server = null
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
