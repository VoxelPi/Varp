package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.FolderPath

class FolderNotFoundException(override val path: FolderPath) : NodeNotFoundException(path)
