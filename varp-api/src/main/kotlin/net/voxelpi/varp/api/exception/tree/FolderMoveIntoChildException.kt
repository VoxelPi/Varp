package net.voxelpi.varp.api.exception.tree

import net.voxelpi.varp.api.warp.path.FolderPath

class FolderMoveIntoChildException(val src: FolderPath, val dst: FolderPath) : Exception()
