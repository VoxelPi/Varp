package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.tree.path.NodeParentPath

public class NodeParentNotFoundException(
    override val path: NodeParentPath,
) : NodeNotFoundException(path)
