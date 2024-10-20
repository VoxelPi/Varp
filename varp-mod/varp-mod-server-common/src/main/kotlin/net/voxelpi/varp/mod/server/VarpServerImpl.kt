package net.voxelpi.varp.mod.server

import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import net.voxelpi.varp.mod.server.player.VarpServerPlayerServiceImpl

interface VarpServerImpl : VarpServerAPI {

    override val playerService: VarpServerPlayerServiceImpl<out VarpServerPlayerImpl>
}
