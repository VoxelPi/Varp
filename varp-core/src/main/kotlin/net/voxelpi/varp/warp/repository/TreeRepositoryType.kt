package net.voxelpi.varp.warp.repository

public interface TreeRepositoryType<R : TreeRepository, C : TreeRepositoryConfig> {

    /**
     * The id of the type.
     */
    public val id: String

    /**
     * Creates a new repository
     */
    public fun createRepository(id: String, config: C): R
}
