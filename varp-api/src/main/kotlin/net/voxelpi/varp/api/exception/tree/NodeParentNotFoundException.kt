package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.NodeParentPath

class NodeParentNotFoundException(override val path: NodeParentPath) : NodeNotFoundException(path)
