package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.warp.path.NodeParentPath

public class NodeParentNotFoundException(
    override val path: NodeParentPath,
) : NodeNotFoundException(path)
