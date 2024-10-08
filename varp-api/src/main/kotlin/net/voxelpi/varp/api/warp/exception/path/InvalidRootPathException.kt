package net.voxelpi.varp.api.warp.exception.path

class InvalidRootPathException(val path: String) : Exception("Invalid module path: \"$path\"")
