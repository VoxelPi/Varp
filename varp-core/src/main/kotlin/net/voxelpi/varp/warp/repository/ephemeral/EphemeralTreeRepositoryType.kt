package net.voxelpi.varp.warp.repository.ephemeral

import net.voxelpi.varp.warp.repository.TreeRepositoryType

public object EphemeralTreeRepositoryType : TreeRepositoryType<EphemeralTreeRepository, EphemeralTreeRepositoryConfig>("ephemeral", EphemeralTreeRepositoryConfig::class.java) {

    override fun createRepository(id: String, config: EphemeralTreeRepositoryConfig): EphemeralTreeRepository {
        return EphemeralTreeRepository(id)
    }
}
