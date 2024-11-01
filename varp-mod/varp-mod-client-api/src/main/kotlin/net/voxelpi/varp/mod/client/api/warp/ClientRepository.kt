package net.voxelpi.varp.mod.client.api.warp

import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.warp.repository.Repository

public abstract class ClientRepository(id: String) : Repository(id) {

    /**
     * If the client is currently is on a server with a supported varp implementation.
     */
    public abstract val active: Boolean

    /**
     * Information about the varp server mod on the connected server.
     */
    public abstract val serverInfo: VarpServerInformation?
}
