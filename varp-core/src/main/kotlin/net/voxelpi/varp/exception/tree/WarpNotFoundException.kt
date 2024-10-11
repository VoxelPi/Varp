package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.warp.path.WarpPath

public class WarpNotFoundException(
    override val path: WarpPath,
) : NodeNotFoundException(path)
