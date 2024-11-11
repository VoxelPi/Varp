package net.voxelpi.varp.warp.repository.ephemeral

import net.voxelpi.varp.warp.repository.RepositoryType

public object EphemeralRepositoryType : RepositoryType<EphemeralRepository, EphemeralRepositoryConfig>("ephemeral", EphemeralRepository::class, EphemeralRepositoryConfig::class) {

    override fun create(id: String, config: EphemeralRepositoryConfig): Result<EphemeralRepository> {
        return Result.success(EphemeralRepository(id, config))
    }
}
