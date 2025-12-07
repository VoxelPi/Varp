package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.tree.path.WarpPath

public class WarpAlreadyExistsException(
    override val path: WarpPath,
) : NodeAlreadyExistsException(path)
