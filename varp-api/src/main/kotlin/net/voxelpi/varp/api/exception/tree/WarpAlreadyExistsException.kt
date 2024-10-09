package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.WarpPath

class WarpAlreadyExistsException(override val path: WarpPath) : NodeAlreadyExistsException(path)
