package net.voxelpi.varp.api.exception.path

class InvalidWarpPathException(val path: String) : Exception("Invalid warp path: \"$path\"")
