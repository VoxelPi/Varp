package net.voxelpi.varp.api.exception.path

class InvalidNodeChildPathException(val path: String) : Exception("Invalid node child path: \"$path\"")
