package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.NodePath

open class NodeNotFoundException(open val path: NodePath) : Exception()
