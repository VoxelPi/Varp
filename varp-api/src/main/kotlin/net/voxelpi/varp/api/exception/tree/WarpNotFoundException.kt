package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.WarpPath

class WarpNotFoundException(override val path: WarpPath) : NodeNotFoundException(path)
