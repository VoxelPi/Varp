package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.tree.path.NodeChildPath

public class NodeChildAlreadyExistsException(
    override val path: NodeChildPath,
) : NodeAlreadyExistsException(path)
