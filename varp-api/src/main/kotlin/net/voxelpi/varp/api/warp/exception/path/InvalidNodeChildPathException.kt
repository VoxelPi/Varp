package net.voxelpi.varp.api.warp.exception.path

class InvalidNodeChildPathException(val path: String) : Exception("Invalid node child path: \"$path\"")
