package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.tree.path.NodeParentPath

public class NodeParentAlreadyExistsException(
    override val path: NodeParentPath,
) : NodeAlreadyExistsException(path)
