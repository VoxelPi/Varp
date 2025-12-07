package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.tree.path.NodeChildPath

public class NodeChildNotFoundException(
    override val path: NodeChildPath,
) : NodeNotFoundException(path)
