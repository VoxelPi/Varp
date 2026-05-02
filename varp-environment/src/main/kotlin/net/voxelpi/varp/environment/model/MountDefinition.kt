package net.voxelpi.varp.environment.model

import net.voxelpi.varp.repository.compositor.CompositorMount
import net.voxelpi.varp.tree.path.NodeParentPath

@JvmRecord
public data class MountDefinition(
    val repository: String,
    val path: NodeParentPath,
    val overlay: CompositorMount.Overlay,
) {

    public constructor(
        repository: String,
        sourcePath: NodeParentPath,
        overlayBuilder: CompositorMount.Overlay.Builder.() -> Unit,
    ) : this(repository, sourcePath, CompositorMount.Overlay.Builder().apply(overlayBuilder).build())
}
