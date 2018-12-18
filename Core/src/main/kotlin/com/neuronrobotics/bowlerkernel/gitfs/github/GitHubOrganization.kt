package com.neuronrobotics.bowlerkernel.gitfs.github

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.gitfs.GitRepository

/**
 * A GitHub organization.
 */
interface GitHubOrganization {

    fun getRepositories(): ImmutableList<GitRepository>

}
