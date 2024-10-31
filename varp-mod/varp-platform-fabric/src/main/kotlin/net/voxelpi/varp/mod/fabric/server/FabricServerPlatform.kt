package net.voxelpi.varp.mod.fabric.server

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.voxelpi.varp.mod.server.ServerPlatformImpl

class FabricServerPlatform : ServerPlatformImpl {

    override val isDedicated: Boolean = FabricLoader.getInstance().environmentType == EnvType.SERVER
}
