package net.voxelpi.varp.environment

import net.voxelpi.varp.repository.Storage
import net.voxelpi.varp.repository.StorageHandle
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState

@JvmRecord
public data class EnvironmentDefinition(
    val repositories: Map<String, RepositoryDefinition<*, *>>,
    val mounts: Map<NodeParentPath, MountDefinition>,
) {

    public class Builder internal constructor() {

        public val repositories: MutableMap<String, RepositoryDefinition<*, *>> = mutableMapOf()
        public val mounts: MutableMap<NodeParentPath, MountDefinition> = mutableMapOf()

        public fun <C : Any, H : StorageHandle> repository(
            id: String,
            storage: Storage<C, H>,
            config: C,
            builder: RepositoryBuilder.() -> Unit = {},
        ): RepositoryDefinition<C, H> {
            val (repositoryMounts, defaultState) = RepositoryBuilder(id).apply(builder).build()
            mounts.putAll(repositoryMounts)

            val repositoryDefinition = RepositoryDefinition(storage.createInstance(config), defaultState)
            repositories[id] = repositoryDefinition
            return repositoryDefinition
        }

        public fun <H : StorageHandle> repository(
            id: String,
            storage: Storage<Unit, H>,
            builder: RepositoryBuilder.() -> Unit = {},
        ): RepositoryDefinition<Unit, H> {
            val (repositoryMounts, defaultState) = RepositoryBuilder(id).apply(builder).build()
            mounts.putAll(repositoryMounts)

            val repositoryDefinition = RepositoryDefinition(storage.createInstance(Unit), defaultState)
            repositories[id] = repositoryDefinition
            return repositoryDefinition
        }

        internal fun build(): EnvironmentDefinition {
            return EnvironmentDefinition(repositories, mounts)
        }

        public class RepositoryBuilder internal constructor(
            private val repositoryId: String,
        ) {
            public val mounts: MutableMap<NodeParentPath, MountDefinition> = mutableMapOf()

            public var defaultState: TreeState = MutableTreeState()

            public fun mountedAt(location: NodeParentPath, path: NodeParentPath = RootPath, state: FolderState = FolderState.defaultMountState()) {
                mounts[location] = MountDefinition(repositoryId, path, state)
            }

            public fun mountedAt(location: String, path: String = RootPath.toString(), state: FolderState = FolderState.defaultMountState()) {
                val locationPath = NodeParentPath.parse(location).getOrThrow()
                val pathPath = NodeParentPath.parse(path).getOrThrow()
                return mountedAt(locationPath, pathPath, state)
            }

            internal fun build(): Pair<Map<NodeParentPath, MountDefinition>, TreeState> {
                return Pair(mounts, defaultState)
            }
        }
    }

    public companion object {

        public fun environmentDefinition(builder: Builder.() -> Unit): EnvironmentDefinition {
            val builder = Builder()
            builder.builder()
            return builder.build()
        }
    }
}
