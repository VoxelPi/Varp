package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.warp.path.NodePath

public open class NodeAlreadyExistsException(
    public open val path: NodePath,
) : Exception("A node with the path \"$path\" already exists.")
