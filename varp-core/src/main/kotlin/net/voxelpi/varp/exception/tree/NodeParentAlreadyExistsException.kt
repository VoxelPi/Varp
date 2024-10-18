package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.warp.path.NodeParentPath

public class NodeParentAlreadyExistsException(
    override val path: NodeParentPath,
) : NodeAlreadyExistsException(path)
