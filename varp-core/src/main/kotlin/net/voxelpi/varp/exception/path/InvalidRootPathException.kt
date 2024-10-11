package net.voxelpi.varp.exception.path

public class InvalidRootPathException(
    public val path: String,
) : Exception("Invalid module path: \"$path\"")
