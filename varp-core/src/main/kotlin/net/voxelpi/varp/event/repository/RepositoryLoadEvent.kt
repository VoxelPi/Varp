package net.voxelpi.varp.event.repository

import net.voxelpi.varp.repository.Repository

@JvmRecord
public data class RepositoryLoadEvent(
    override val repository: Repository,
) : RepositoryEvent
