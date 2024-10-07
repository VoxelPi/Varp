package net.voxelpi.varp.api.warp.exception.path

class InvalidModulePathException(val path: String) : Exception("Invalid module path: \"$path\"")
