package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.tree.path.FolderPath

public class FolderMoveIntoChildException(
    public val src: FolderPath,
    public val dst: FolderPath,
) : Exception("Can't move folder \"$src\" into its child folder \"${dst.parent}\"")
