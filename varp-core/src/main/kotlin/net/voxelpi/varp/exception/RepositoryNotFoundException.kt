package net.voxelpi.varp.exception

public class RepositoryNotFoundException(
    public val id: String,
) : Exception("No repository with the id \"$id\" exists.")
