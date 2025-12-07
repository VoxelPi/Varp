package net.voxelpi.varp.repository.ephemeral

import net.voxelpi.varp.repository.RepositoryType

public object EphemeralRepositoryType : RepositoryType<EphemeralRepository, EphemeralRepositoryConfig>("ephemeral", EphemeralRepository::class, EphemeralRepositoryConfig::class) {

    override fun create(id: String, config: EphemeralRepositoryConfig): Result<EphemeralRepository> {
        return Result.success(EphemeralRepository(id, config))
    }
}
