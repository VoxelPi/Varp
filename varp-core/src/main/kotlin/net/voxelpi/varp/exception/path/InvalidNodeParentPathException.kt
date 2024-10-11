package net.voxelpi.varp.exception.path

public class InvalidNodeParentPathException(
    public val path: String,
) : Exception("Invalid node parent path: \"$path\"")
