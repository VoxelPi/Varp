package net.voxelpi.varp.api.exception.path

class InvalidFolderPathException(val path: String) : Exception("Invalid folder path: \"$path\"")
