/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.gitfs.github

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.gitfs.GitRepository

/**
 * A GitHub organization.
 */
interface GitHubOrganization {

    fun getRepositories(): ImmutableList<GitRepository>

}
