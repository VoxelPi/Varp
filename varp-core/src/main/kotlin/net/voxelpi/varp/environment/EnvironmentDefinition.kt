package net.voxelpi.varp.environment

import net.voxelpi.varp.compositor.CompositorMount
import net.voxelpi.varp.repository.Storage
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath

@JvmRecord
public data class EnvironmentDefinition(
    val repositories: Map<String, RepositoryDefinition<*, *>>,
    val mounts: Map<NodeParentPath, MountDefinition>,
) {

    public class Builder internal constructor() {

        public val repositories: MutableMap<String, RepositoryDefinition<*, *>> = mutableMapOf()
        public val mounts: MutableMap<NodeParentPath, MountDefinition> = mutableMapOf()

        public fun <C : Any, H : Any> repository(
            id: String,
            storage: Storage<C, H>,
            config: C,
            builder: RepositoryBuilder.() -> Unit = {},
        ): RepositoryDefinition<C, H> {
            val repositoryDefinition = RepositoryDefinition(storage.createInstance(config))
            repositories[id] = repositoryDefinition

            val repositoryMounts = RepositoryBuilder(id).apply(builder).build()
            mounts.putAll(repositoryMounts)

            return repositoryDefinition
        }

        public fun <H : Any> repository(
            id: String,
            storage: Storage<Unit, H>,
            builder: RepositoryBuilder.() -> Unit = {},
        ): RepositoryDefinition<Unit, H> {
            val repositoryDefinition = RepositoryDefinition(storage.createInstance(Unit))
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

            public fun mountedAt(location: NodeParentPath, path: NodeParentPath = RootPath, overlayBuilder: CompositorMount.Overlay.Builder.() -> Unit) {
                mounts[location] = MountDefinition(repositoryId, path, overlayBuilder)
            }

            public fun mountedAt(location: String, path: String = RootPath.toString(), overlayBuilder: CompositorMount.Overlay.Builder.() -> Unit) {
                val locationPath = NodeParentPath.parse(location).getOrThrow()
                val pathPath = NodeParentPath.parse(path).getOrThrow()
                return mountedAt(locationPath, pathPath, overlayBuilder)
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
