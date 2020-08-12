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
package com.commonwealthrobotics.bowlerkernel.proto

import com.commonwealthrobotics.proto.gitfs.ClearCacheRequest
import com.commonwealthrobotics.proto.gitfs.ClearCacheResponse
import com.commonwealthrobotics.proto.gitfs.FileSpec
import com.commonwealthrobotics.proto.gitfs.ProjectSpec
import com.google.protobuf.ByteString

fun projectSpec(repoRemote: String, revision: String, patch: ByteArray) = ProjectSpec.newBuilder().apply {
    setRepoRemote(repoRemote)
    setRevision(revision)
    setPatch(ByteString.copyFrom(patch))
}.build()

fun fileSpec(project: ProjectSpec, path: String) = FileSpec.newBuilder().apply {
    setProject(project)
    setPath(path)
}.build()

fun clearCacheRequest() = ClearCacheRequest.newBuilder().build()

fun clearCacheResponse() = ClearCacheResponse.newBuilder().build()
