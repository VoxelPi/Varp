package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.warp.path.FolderPath

public class FolderMoveIntoChildException(
    public val src: FolderPath,
    public val dst: FolderPath,
) : Exception()
