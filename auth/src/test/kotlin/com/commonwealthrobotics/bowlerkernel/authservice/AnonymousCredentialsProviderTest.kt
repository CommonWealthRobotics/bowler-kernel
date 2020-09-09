package com.commonwealthrobotics.bowlerkernel.authservice

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class AnonymousCredentialsProviderTest {

    @Test
    fun `credentials returns anonymous`() {
        runBlocking {
            AnonymousCredentialsProvider.getCredentialsFor("").shouldBe(Credentials.Anonymous)
        }
    }

    @Test
    fun `cannot request 2FA code for anonymous`() {
        runBlocking {
            shouldThrow<UnsupportedOperationException> {
                AnonymousCredentialsProvider.getTwoFactorFor("")
            }
        }
    }
}
