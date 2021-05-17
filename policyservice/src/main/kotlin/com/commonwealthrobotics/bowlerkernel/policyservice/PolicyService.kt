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
import com.commonwealthrobotics.proto.policy.PolicyDocument
import com.commonwealthrobotics.proto.policy.PolicyServiceGrpcKt
import com.commonwealthrobotics.proto.policy.SetPolicyDocumentResponse
import com.google.protobuf.ProtocolStringList
import mu.KotlinLogging
import java.text.ParseException
import java.text.SimpleDateFormat

class PolicyService : PolicyServiceGrpcKt.PolicyServiceCoroutineImplBase() {

    /**
     * This is the currently acting [PolicyDocument]. A new one may only be set by [setPolicyDocument], if it is
     * accepted.
     */
    private var actingPolicyDocument = PolicyDocument.newBuilder().build()

    override suspend fun getPolicyDocument(request: GetPolicyDocumentRequest): PolicyDocument {
        return actingPolicyDocument
    }

    override suspend fun setPolicyDocument(request: PolicyDocument): SetPolicyDocumentResponse {
        try {
            validateVersion(request)
            request.statementsList.forEach {
                when {
                    it.hasActionPolicy() -> validateActionPolicy(it.actionPolicy)
                    it.hasCredentialPolicy() -> validateCredentialPolicy(it.credentialPolicy)
                    else -> error("Unknown policy type.")
                }
            }
        } catch (ex: RuntimeException) {
            logger.debug(ex) { "Rejecting policy: $request" }
            return SetPolicyDocumentResponse.newBuilder().apply {
                accepted = false
                rejectionReason = "Policy did not pass validation Rejection reason: ${ex.localizedMessage}"
            }.build()
        }

        logger.debug {
            """
            |Accepting new policy document
            |$request
            """.trimMargin()
        }
        actingPolicyDocument = request
        return SetPolicyDocumentResponse.newBuilder().apply {
            accepted = true
        }.build()
    }

    /**
     * Returns whether the [actingPolicyDocument] allows the given action to be performed on the given resource.
     *
     * @param actionComponents The components of the action.
     * @param resourceComponents The components of the resource.
     * @return Whether the action is allowed to be performed on the resource.
     * @see [PolicyDocument]
     */
    fun isActionAllowed(actionComponents: List<String>, resourceComponents: List<String>): Boolean {
        val policyList = actingPolicyDocument.statementsList
            .filter { it.hasActionPolicy() }
            .map { it.actionPolicy }
            .map {
                // Eliminate actions and resources that don't match the desired action and resource
                val newActions = it.actionsList.filter { matchComponents(actionComponents, components(it)) }
                val newResources = it.resourcesList.filter { matchComponents(resourceComponents, components(it)) }
                it.toBuilder()
                    .clearActions()
                    .clearResources()
                    .addAllActions(newActions)
                    .addAllResources(newResources)
                    .build()
            }
            .filter {
                // Discard action policies that don't match the desired action and resource
                it.actionsList.isNotEmpty() && it.resourcesList.isNotEmpty()
            }

        // If there are no matching credential policies, then the scope cannot be authenticated to.
        // If any action policies matched, then the action is allowed.
        return policyList.isNotEmpty()
    }

    /**
     * Returns the components of the credentials that must be used to authenticate to the given scope.
     * The components of [CredentialPolicy.getCredentials] will be returned for the most specific relevant
     * [CredentialPolicy] in the [actingPolicyDocument].
     *
     * @param scopeComponents The scope's components.
     * @return The credentials components.
     * @see [PolicyDocument]
     */
    fun credentialsForScope(scopeComponents: List<String>): List<String> {
        val policyList = actingPolicyDocument.statementsList
            .filter { it.hasCredentialPolicy() }
            .map { it.credentialPolicy }
            .map {
                // Eliminate scopes that don't match the desired scope
                val newScopes = it.scopesList.filter { matchComponents(scopeComponents, components(it)) }
                it.toBuilder().clearScopes().addAllScopes(newScopes).build()
            }
            .filter {
                // Discard credential policies that don't match the desired scope
                it.scopesList.isNotEmpty()
            }

        // If there are no matching credential policies, then the scope cannot be authenticated to.
        if (policyList.isEmpty()) {
            return listOf()
        }

        // Break ties between policies by choosing the more specific one.
        val policy = policyList.reduce { policy1, policy2 ->
            if (policy1.credentials == "anonymous" && policy2.credentials != "anonymous") {
                policy2
            } else if (policy1.credentials != "anonymous" && policy2.credentials == "anonymous") {
                policy1
            } else {
                policy2
            }
        }

        return components(policy.credentials)
    }

    /**
     * Breaks a [string] into components.
     *
     * @param string The [String] to split into components.
     * @return The components for the `string`.
     * @see [PolicyDocument]
     */
    fun components(string: String): List<String> {
        val components = string.split(':')
        components.forEach { check(!it.contains(Regex("\\*.+"))) }
        return components
    }

    /**
     * @param requestComponents The components in the request that must be matched against the policy.
     * @param policyComponents The components in the policy.
     * @return Whether the request is matched by the policy.
     */
    private fun matchComponents(requestComponents: List<String>, policyComponents: List<String>): Boolean {
        return requestComponents.zip(policyComponents).fold(true) { v, (r, p) ->
            val (p1) = p.split('*')
            v && r.startsWith(p1)
        }
    }

    private fun validateActionPolicy(policy: ActionPolicy) {
        validateActions(policy.actionsList)
        validateResources(policy.resourcesList)

        check(!(policy.actionsList.isNotEmpty() && policy.resourcesList.isEmpty())) {
            "Expected one or more resources to be specified because one or more actions were specified, " +
                "but got zero resources."
        }
    }

    private fun validateActions(actionsList: ProtocolStringList) {
        actionsList.forEach { validateAction(it) }
    }

    private fun validateAction(action: String) {
        val components = components(action)
        when {
            components[0] == "flash" -> validateFlashAction(components)
            components[0] == "*" && components.size == 1 -> return
            else -> throw IllegalStateException("Expected one of `flash` but got `${components[0]}` instead.")
        }
    }

    private fun validateFlashAction(components: List<String>) {
        check(components[0] == "flash") {
            "Expected action to start with `flash` but got `${components[0]}`."
        }
        check(components.size == 2) {
            "Expected action to have 2 components but got `${components.size}`."
        }
        when {
            components[1] == "uc" -> return
            components[1] == "*" && components.size == 2 -> return
            else -> throw IllegalStateException("Expected one of `uc` but got `${components[1]}` instead.")
        }
    }

    private fun validateResources(resourcesList: ProtocolStringList) {
        resourcesList.forEach { validateResource(it) }
    }

    private fun validateResource(resource: String) {
        val components = components(resource)
        when {
            components[0] == "uc" -> validateUcResource(components)
            components[0] == "*" && components.size == 1 -> return
            else -> throw IllegalStateException("Expected one of `uc` but got `${components[0]}` instead.")
        }
    }

    private fun validateUcResource(components: List<String>) {
        when (components[1]) {
            "name", "file", "ipv4" -> return
            "hid" -> {
                val ids = components[2].split('/')
                if (ids.size == 1) {
                    check(ids[0].contains('*')) {
                        "An HID value with one part must contain a wildcard but got `${ids[0]}`."
                    }
                } else if (ids.size > 2) {
                    throw IllegalStateException("Expected two parts in HID value but got `${ids.size}` instead.")
                }
            }
        }
    }

    private fun validateCredentialPolicy(policy: CredentialPolicy) {
        validateCredentials(policy.credentials)
        validateScopes(policy.scopesList)
    }

    private fun validateCredentials(credentials: String) {
        val components = components(credentials)
        when {
            components.size == 1 && components[0] == "anonymous" -> return
            components.size == 2 && components[0] == "api_key" -> return
            else -> throw IllegalStateException("Credentials are not valid: $credentials")
        }
    }

    private fun validateScopes(scopesList: ProtocolStringList) {
        check(scopesList.isNotEmpty()) {
            "Must specify at least one scope."
        }
        scopesList.forEach { validateScope(it) }
    }

    private fun validateScope(scope: String) {
        val components = components(scope)
        when {
            components[0] == "github" -> validateGitHubScope(components)
            components[0] == "*" && components.size == 1 -> return
            else -> throw IllegalStateException("Policy scope is not valid: $scope")
        }
    }

    private fun validateGitHubScope(components: List<String>) {
        check(components[0] == "github") {
            "Expected scope to start with `github` but got `${components[0]}`."
        }
        check(components.size == 3) {
            "Expected scope to contain 3 components but got `${components.size}`."
        }
        when {
            components[1] == "repo" -> return
            components[1] == "gist" -> return
            else -> throw IllegalStateException("GitHub scope is not valid: ${components.joinToString(":")}")
        }
    }

    private fun validateVersion(request: PolicyDocument) {
        try {
            dateFormat.parse(request.version)
        } catch (ex: ParseException) {
            throw IllegalStateException(
                "Policy version must be a valid date in the format yyyy-mm-dd. Got `${request.version}`.",
                ex
            )
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    }
}
