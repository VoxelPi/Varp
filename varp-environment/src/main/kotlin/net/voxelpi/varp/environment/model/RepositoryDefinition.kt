package net.voxelpi.varp.environment.model

import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.repository.RepositoryConfig
import net.voxelpi.varp.repository.RepositoryType

@JvmRecord
public data class RepositoryDefinition<C : RepositoryConfig>(
    val type: RepositoryType<*, C>,
    val config: C,
) {
    public companion object {
        public fun repositoryDefinition(repository: Repository): RepositoryDefinition<RepositoryConfig> {
            @Suppress("UNCHECKED_CAST")
            return RepositoryDefinition(repository.type as RepositoryType<Repository, RepositoryConfig>, repository.config)
        }
    }
}
