package net.voxelpi.varp.warp.repository

/**
 * The type of tree repository.
 *
 * @property id the id of the type.
 * @property configType the type of the config.
 */
public abstract class TreeRepositoryType<R : TreeRepository, C : TreeRepositoryConfig>(
    public val id: String,
    public val configType: Class<C>,
) {

    /**
     * Creates a new repository
     */
    public abstract fun createRepository(id: String, config: C): R
}
