package net.voxelpi.varp.repository.compositor

import net.kyori.adventure.text.Component
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.NodeParentPath

/**
 * Specifies a mount of a tree compositor.
 * @property path The path where the repository should be mounted.
 * @property repository The repository that should be mounted.
 * @property sourcePath The path of the container in the repository that is mounted to the tree.
 */
public data class CompositorMount(
    val path: NodeParentPath,
    val repository: Repository,
    val sourcePath: NodeParentPath,
    val overlay: Overlay,
) {

    public constructor(
        path: NodeParentPath,
        repository: Repository,
        sourcePath: NodeParentPath,
        overlayBuilder: Overlay.Builder.() -> Unit,
    ) : this(path, repository, sourcePath, Overlay.Builder().apply(overlayBuilder).build())

    public data class Overlay(
        val name: Component?,
    ) {

        public data class Builder(
            var name: Component? = null,
        ) {

            public fun build(): Overlay {
                return Overlay(name)
            }
        }
    }
}
