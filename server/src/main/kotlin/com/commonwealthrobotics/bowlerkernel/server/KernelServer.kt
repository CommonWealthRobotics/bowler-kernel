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

import com.commonwealthrobotics.bowlerkernel.di.BowlerKernelKoinContext
import com.commonwealthrobotics.bowlerkernel.gitfs.DefaultDependencyResolver
import com.commonwealthrobotics.bowlerkernel.gitfs.DependencyResolver
import com.commonwealthrobotics.bowlerkernel.scripthost.ScriptHost
import com.commonwealthrobotics.bowlerkernel.scripting.DefaultScriptLoader
import com.commonwealthrobotics.bowlerkernel.scripting.ScriptLoader
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import mu.KotlinLogging
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class KernelServer {

    private lateinit var server: Server

    val port
        get() = server.port

    fun start() {
        BowlerKernelKoinContext.koinApp = koinApplication {
            modules(
                module {
                    factory<DependencyResolver> { DefaultDependencyResolver(get()) }
                    factory<ScriptLoader> { DefaultScriptLoader(get()) }
                }
            )
        }

        server = NettyServerBuilder.forPort(0).apply {
            // TODO: Support SSL
            addService(ScriptHost())
        }.build()
        server.start()
        logger.info { "Server running on port ${server.port}" }
    }

    fun stop(): Unit = server.shutdown().awaitTermination()

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
