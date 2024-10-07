package net.voxelpi.varp.api.warp.exception.path

class InvalidWarpPathException(val path: String) : Exception("Invalid warp path: \"$path\"")
