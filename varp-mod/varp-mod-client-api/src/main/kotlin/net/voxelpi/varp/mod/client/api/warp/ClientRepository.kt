package net.voxelpi.varp.mod.client.api.warp

import net.voxelpi.varp.repository.Repository

public abstract class ClientRepository(id: String) : Repository(id) {

    override val config: ClientRepositoryConfig
        get() = ClientRepositoryConfig

    override val type: ClientRepositoryType
        get() = ClientRepositoryType

    /**
     * If the client is currently is on a server with a supported varp implementation.
     */
    public abstract val active: Boolean
}
