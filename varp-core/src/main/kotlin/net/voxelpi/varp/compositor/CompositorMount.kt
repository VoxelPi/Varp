package net.voxelpi.varp.compositor

import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.NodeParentPath

/**
 * Specifies a mount of a tree compositor.
 * @property targetPath The path where the repository should be mounted.
 * @property repository The repository that should be mounted.
 * @property sourcePath The path of the container in the repository that is mounted to the tree.
 */
@JvmRecord
public data class CompositorMount(
    val targetPath: NodeParentPath,
    val repository: Repository<*, *>,
    val sourcePath: NodeParentPath,
    val overlay: Overlay,
) {

    public constructor(
        targetPath: NodeParentPath,
        repository: Repository<*, *>,
        sourcePath: NodeParentPath,
        overlayBuilder: Overlay.Builder.() -> Unit,
    ) : this(targetPath, repository, sourcePath, Overlay.Builder().apply(overlayBuilder).build())

    @JvmRecord
    public data class Overlay(
        val name: ComponentTemplate?,
    ) {

        public data class Builder(
            var name: ComponentTemplate? = null,
        ) {

            public fun build(): Overlay {
                return Overlay(name)
            }
        }
    }
}
