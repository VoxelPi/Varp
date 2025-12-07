package net.voxelpi.varp.repository

import kotlin.reflect.KClass

/**
 * The type of repository.
 *
 * @property id The id of the repository type.
 * @property repositoryType The class of the repository.
 * @property configType The class of the repository config.
 */
public abstract class RepositoryType<R : Repository, C : RepositoryConfig>(
    public val id: String,
    public val repositoryType: KClass<R>,
    public val configType: KClass<C>,
) {

    /**
     * Creates a new repository with the given [id] and [config].
     */
    public abstract fun create(id: String, config: C): Result<R>
}
