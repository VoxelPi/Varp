package net.voxelpi.varp.api.warp.exception.path

class InvalidNodeParentPathException(val path: String) : Exception("Invalid node parent path: \"$path\"")
