package net.voxelpi.varp.exception.path

public class InvalidWarpPathException(
    public val path: String,
) : Exception("Invalid warp path: \"$path\"")
