package net.voxelpi.varp.environment.model

import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.repository.RepositoryConfig
import net.voxelpi.varp.repository.RepositoryType
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath

@JvmRecord
public data class EnvironmentDefinition(
    val repositories: Map<String, RepositoryDefinition<*>>,
    val mounts: Map<NodeParentPath, MountDefinition>,
) {

    public class Builder internal constructor() {

        public val repositories: MutableMap<String, RepositoryDefinition<*>> = mutableMapOf()
        public val mounts: MutableMap<NodeParentPath, MountDefinition> = mutableMapOf()

        public fun <R : Repository, C : RepositoryConfig> repository(
            id: String,
            type: RepositoryType<R, C>,
            config: C,
            builder: RepositoryBuilder.() -> Unit = {},
        ): RepositoryDefinition<C> {
            val repositoryDefinition = RepositoryDefinition(type, config)
            repositories[id] = repositoryDefinition

            val repositoryMounts = RepositoryBuilder(id).apply(builder).build()
            mounts.putAll(repositoryMounts)

            return repositoryDefinition
        }

        internal fun build(): EnvironmentDefinition {
            return EnvironmentDefinition(repositories, mounts)
        }

        public class RepositoryBuilder internal constructor(
            private val repositoryId: String,
        ) {
            public val mounts: MutableMap<NodeParentPath, MountDefinition> = mutableMapOf()

            public fun mountedAt(location: NodeParentPath, path: NodeParentPath = RootPath) {
                mounts[location] = MountDefinition(repositoryId, path)
            }

            public fun mountedAt(location: String, path: String = RootPath.toString()) {
                val locationPath = NodeParentPath.parse(location).getOrThrow()
                val pathPath = NodeParentPath.parse(path).getOrThrow()
                return mountedAt(locationPath, pathPath)
            }

            internal fun build(): Map<NodeParentPath, MountDefinition> {
                return mounts
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
