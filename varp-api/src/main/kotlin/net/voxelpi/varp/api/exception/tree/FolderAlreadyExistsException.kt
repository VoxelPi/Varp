package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.FolderPath

class FolderAlreadyExistsException(override val path: FolderPath) : NodeAlreadyExistsException(path)
