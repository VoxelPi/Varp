package net.voxelpi.varp.api.warp.exception.path

class InvalidFolderPathException(val path: String) : Exception("Invalid folder path: \"$path\"")
