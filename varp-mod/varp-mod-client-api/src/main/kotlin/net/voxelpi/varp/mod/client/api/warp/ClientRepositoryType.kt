package net.voxelpi.varp.mod.client.api.warp

import net.voxelpi.varp.repository.RepositoryType

public data object ClientRepositoryType : RepositoryType<ClientRepository, ClientRepositoryConfig>("client", ClientRepository::class, ClientRepositoryConfig::class) {

    override fun create(id: String, config: ClientRepositoryConfig): Result<ClientRepository> {
        throw NotImplementedError("This operation is not implemented.")
    }
}
