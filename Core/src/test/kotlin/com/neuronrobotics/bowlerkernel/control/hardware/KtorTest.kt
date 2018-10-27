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
