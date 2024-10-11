package net.voxelpi.varp.exception.path

public class InvalidNodeChildPathException(
    public val path: String,
) : Exception("Invalid node child path: \"$path\"")
