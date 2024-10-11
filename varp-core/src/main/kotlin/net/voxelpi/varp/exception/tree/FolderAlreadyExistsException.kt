package net.voxelpi.varp.exception.tree

import net.voxelpi.varp.warp.path.FolderPath

public class FolderAlreadyExistsException(
    override val path: FolderPath,
) : NodeAlreadyExistsException(path)
