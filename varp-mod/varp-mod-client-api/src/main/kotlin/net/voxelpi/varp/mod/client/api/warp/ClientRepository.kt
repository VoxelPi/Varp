package net.voxelpi.varp.mod.client.api.warp

import net.voxelpi.varp.warp.repository.Repository

public interface ClientRepository : Repository {

    /**
     * If the client is currently is on a server with a supported varp implementation.
     */
    public val active: Boolean
}
