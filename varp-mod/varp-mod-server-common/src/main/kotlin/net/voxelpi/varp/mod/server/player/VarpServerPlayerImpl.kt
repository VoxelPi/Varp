package net.voxelpi.varp.mod.server.player

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.player.ServersideClientInformation
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer

abstract class VarpServerPlayerImpl(
    open val server: VarpServerImpl,
) : VarpServerPlayer {

    override var clientInformation: ServersideClientInformation? = null
        protected set
}
