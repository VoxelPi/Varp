package net.voxelpi.varp.mod.fabric.server

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.voxelpi.varp.mod.server.ServerPlatformImpl

class FabricServerPlatform : ServerPlatformImpl {

    override val isDedicated: Boolean = FabricLoader.getInstance().environmentType == EnvType.SERVER

    override val name: String

    override val brand: String

    override val version: String

    init {
        val serverType = if (isDedicated) "dedicated" else "integrated"
        name = "fabric $serverType server"

        val fabricVersion = FabricLoader.getInstance().getModContainer("fabric")
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
        brand = "fabric@$fabricVersion"

        val fabricApiVersion = FabricLoader.getInstance().getModContainer("fabric-api-base")
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
        version = "fabric-api@$fabricApiVersion"
    }
}
