/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class KtorTest {

    @Test
    fun `ktor test`() {
        val client = HttpClient(Apache)
        runBlocking {
            val content = client.get<String>("")
        }
    }
}
