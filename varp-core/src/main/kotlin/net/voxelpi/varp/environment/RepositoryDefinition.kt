package net.voxelpi.varp.environment

import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.repository.StorageHandle
import net.voxelpi.varp.repository.StorageInstance
import net.voxelpi.varp.tree.state.TreeState

@JvmRecord
public data class RepositoryDefinition<C : Any, H : StorageHandle>(
    val storage: StorageInstance<C, H>,
    val defaultState: TreeState,
) {
    public companion object {
        public fun <C : Any, H : StorageHandle> repositoryDefinition(repository: Repository<C, H>): RepositoryDefinition<C, H> {
            return RepositoryDefinition(repository.storage, repository.defaultState)
        }
    }
}
