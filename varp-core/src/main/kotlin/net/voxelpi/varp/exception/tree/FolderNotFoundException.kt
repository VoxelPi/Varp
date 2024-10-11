package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.warp.path.FolderPath

public class FolderNotFoundException(
    override val path: FolderPath,
) : NodeNotFoundException(path)
