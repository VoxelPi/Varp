package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.tree.path.NodePath

public open class NodeNotFoundException(
    public open val path: NodePath,
) : Exception("No node with the path \"$path\" exists.")
