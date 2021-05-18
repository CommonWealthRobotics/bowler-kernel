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
package com.commonwealthrobotics.bowlerkernel.policyservice

import com.commonwealthrobotics.proto.policy.ActionPolicy
import com.commonwealthrobotics.proto.policy.CredentialPolicy
import com.commonwealthrobotics.proto.policy.GetPolicyDocumentRequest
import com.commonwealthrobotics.proto.policy.PolicyContainer
import com.commonwealthrobotics.proto.policy.PolicyDocument
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class PolicyServiceTest {

    @Test
    fun `empty policy if none has been set`() {
        val service = PolicyService()
        runBlocking {
            service.getPolicyDocument(GetPolicyDocumentRequest.newBuilder().build()).shouldBe(
                PolicyDocument.newBuilder().build()
            )
        }
    }

    @Test
    fun `set and get an empty policy document`() {
        val service = PolicyService()
        runBlocking {
            val document = PolicyDocument.newBuilder().apply {
                version = "2021-04-01"
            }.build()

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.getPolicyDocument(GetPolicyDocumentRequest.newBuilder().build()).shouldBe(document)
        }
    }

    @Test
    fun `anonymous credentials for all scopes`() {
        val service = PolicyService()
        runBlocking {
            val document = credentialPolicy {
                credentials = "anonymous"
                addScopes("*")
            }

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.credentialsForScope(listOf("github", "repo", "MyOrg/MyRepo")).shouldContainExactly("anonymous")
        }
    }

    @Test
    fun `anonymous credentials for github org`() {
        val service = PolicyService()
        runBlocking {
            val document = credentialPolicy {
                credentials = "anonymous"
                addScopes("github:repo:MyOrg/*")
            }

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.credentialsForScope(listOf("github", "repo", "MyOrg/MyRepo")).shouldContainExactly("anonymous")
        }
    }

    @Test
    fun `no credentials for github org`() {
        val service = PolicyService()
        runBlocking {
            val document = credentialPolicy {
                credentials = "anonymous"
                addScopes("github:repo:MyOrg/*")
            }

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.credentialsForScope(listOf("github", "repo", "MyOtherOrg/MyRepo")).shouldBeEmpty()
        }
    }

    @Test
    fun `api key over anonymous auth`() {
        val service = PolicyService()
        runBlocking {
            val document = PolicyDocument.newBuilder().apply {
                version = "2021-04-17"
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        credentialPolicyBuilder.credentials = "anonymous"
                        credentialPolicyBuilder.addScopes("*")
                    }
                )
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        credentialPolicyBuilder.credentials = "api_key:sd9fsd089f90sdf"
                        credentialPolicyBuilder.addScopes("*")
                    }
                )
            }.build()

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.credentialsForScope(listOf("github", "repo", "MyOrg/MyRepo")).shouldContainExactly(
                "api_key",
                "sd9fsd089f90sdf"
            )
        }
    }

    @Test
    fun `api key over anonymous auth for github repo`() {
        val service = PolicyService()
        runBlocking {
            val document = PolicyDocument.newBuilder().apply {
                version = "2021-04-17"
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        credentialPolicyBuilder.credentials = "api_key:sd9fsd089f90sdf"
                        credentialPolicyBuilder.addScopes("github:repo:MyOrg/*")
                    }
                )
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        credentialPolicyBuilder.credentials = "anonymous"
                        credentialPolicyBuilder.addScopes("*")
                    }
                )
            }.build()

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.credentialsForScope(listOf("github", "repo", "MyOrg/MyRepo")).shouldContainExactly(
                "api_key",
                "sd9fsd089f90sdf"
            )
            service.credentialsForScope(listOf("github", "repo", "MyOtherOrg/MyRepo")).shouldContainExactly("anonymous")
        }
    }

    @Test
    fun `the latter of two api keys is chosen`() {
        val service = PolicyService()
        runBlocking {
            val document = PolicyDocument.newBuilder().apply {
                version = "2021-04-17"
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        credentialPolicyBuilder.credentials = "api_key:sd9fsd089f90sdf"
                        credentialPolicyBuilder.addScopes("github:repo:MyOrg/MyRepo")
                    }
                )
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        credentialPolicyBuilder.credentials = "api_key:bv7nvbn89v8n"
                        credentialPolicyBuilder.addScopes("github:repo:MyOrg/MyRepo")
                    }
                )
            }.build()

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.credentialsForScope(listOf("github", "repo", "MyOrg/MyRepo")).shouldContainExactly(
                "api_key",
                "bv7nvbn89v8n"
            )
        }
    }

    @Test
    fun `example flash uc is allowed`() {
        val service = PolicyService()
        runBlocking {
            val document = actionPolicy {
                addActions("flash:uc")
                addResources("*")
            }

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.isActionAllowed(listOf("flash", "uc"), listOf("uc", "name", "my-uc-name")).shouldBeTrue()
            service.isActionAllowed(listOf("flash", "ab"), listOf("uc", "name", "my-uc-name")).shouldBeFalse()
        }
    }

    @Test
    fun `flash any in any dev file`() {
        val service = PolicyService()
        runBlocking {
            val document = actionPolicy {
                addActions("flash:*")
                addResources("uc:file:/dev/*")
            }

            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }

            service.isActionAllowed(listOf("flash", "uc"), listOf("uc", "file", "/dev/tty1")).shouldBeTrue()
            service.isActionAllowed(listOf("flash", "uc"), listOf("uc", "file", "/dev/sda1")).shouldBeTrue()
            service.isActionAllowed(listOf("flash", "uc"), listOf("uc", "file", "/notdev/sda1")).shouldBeFalse()
            service.isActionAllowed(listOf("flash", "uc"), listOf("uc", "name", "my-uc-name")).shouldBeFalse()
            service.isActionAllowed(listOf("flash", "uc"), listOf("uc", "ipv4", "192.168.1.2")).shouldBeFalse()
            service.isActionAllowed(listOf("flash", "uc"), listOf("uc", "hid", "ab12/cd34")).shouldBeFalse()
        }
    }

    @ParameterizedTest
    @MethodSource("validPolicyDocuments")
    fun `test valid policy documents`(document: PolicyDocument) {
        val service = PolicyService()
        runBlocking {
            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldBeEmpty()
                it.accepted.shouldBeTrue()
            }
        }
    }

    @ParameterizedTest
    @MethodSource("invalidPolicyDocuments")
    fun `test invalid policy documents`(document: PolicyDocument) {
        val service = PolicyService()
        runBlocking {
            service.setPolicyDocument(document).should {
                it.rejectionReason.shouldNotBeEmpty()
                it.accepted.shouldBeFalse()
            }
        }
    }

    companion object {

        @Suppress("unused", "LongMethod")
        @JvmStatic
        fun validPolicyDocuments() = listOf(
            PolicyDocument.newBuilder().apply {
                version = "2021-04-17"
            }.build(),
            credentialPolicy {
                credentials = "anonymous"
                addScopes("*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:repo:MyOrg/MyRepo")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:repo:MyOrg/*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:repo:*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:gist:*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:gist:ssd7f89s8f*")
            },
            credentialPolicy {
                credentials = "api_key:f7s5nzp98g9dh1312khj35bv9v82n2200vb7fds2"
                addScopes("*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("*")
            },
            actionPolicy {
                addActions("flash:*")
                addResources("*")
            },
            actionPolicy {
                addActions("*")
                addResources("*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:name:*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:name:my-uc-name")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:name:my-*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:file:*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:file:/dev/sda")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:file:/dev/tty*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:ipv4:*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:ipv4:192.168.1.2")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:ipv4:192.168.*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:hid:*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:hid:ab12/cd34")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:hid:ab12/*")
            },
        )

        @Suppress("unused", "LongMethod")
        @JvmStatic
        fun invalidPolicyDocuments() = listOf(
            PolicyDocument.newBuilder().apply {
                version = "some-version"
            }.build(),
            credentialPolicy {
            },
            credentialPolicy {
                credentials = ""
            },
            credentialPolicy {
                credentials = "anonymous"
            },
            credentialPolicy {
                credentials = "api_key:asjhdashkjdasjkh"
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("*:*:*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("**")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("*")
                addScopes("*:*:*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("*:*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:*")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:repo:MyOrg/*Repo")
            },
            credentialPolicy {
                credentials = "anonymous"
                addScopes("github:gist:ssd7f89s8f*s9f90")
            },
            credentialPolicy {
                credentials = "some-creds"
                addScopes("*")
            },
            credentialPolicy {
                credentials = "api_key:*"
                addScopes("*:*:*")
            },
            credentialPolicy {
                credentials = "*"
                addScopes("*:*:*")
            },
            actionPolicy {
                addActions("flash:uc")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:hid:ab12cd34")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:ipv4:192.168.*.*")
            },
            actionPolicy {
                addActions("flash:uc")
                addResources("uc:ipv4:192.168.*.1")
            },
        )

        private fun credentialPolicy(builder: CredentialPolicy.Builder.() -> Unit) =
            PolicyDocument.newBuilder().apply {
                version = "2021-04-17"
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        credentialPolicyBuilder.apply(builder)
                    }
                )
            }.build()

        private fun actionPolicy(builder: ActionPolicy.Builder.() -> Unit) =
            PolicyDocument.newBuilder().apply {
                version = "2021-04-17"
                addStatements(
                    PolicyContainer.newBuilder().apply {
                        actionPolicyBuilder.apply(builder)
                    }
                )
            }.build()
    }
}
