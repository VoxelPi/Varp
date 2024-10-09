package net.voxelpi.varp.api.exception.path

class InvalidNodeParentPathException(val path: String) : Exception("Invalid node parent path: \"$path\"")
