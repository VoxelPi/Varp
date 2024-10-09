package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.NodePath

open class NodeAlreadyExistsException(open val path: NodePath) : Exception()
