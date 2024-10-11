package net.voxelpi.varp.exception.path

public class InvalidFolderPathException(
    public val path: String,
) : Exception("Invalid folder path: \"$path\"")
