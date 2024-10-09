package net.voxelpi.varp.api.exception.path

class InvalidRootPathException(val path: String) : Exception("Invalid module path: \"$path\"")
