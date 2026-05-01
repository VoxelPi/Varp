package net.voxelpi.varp.mod.fabric.server.command

import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.kotlin.extension.cloudKey

object FabricVarpCommandArguments {

    val SERVER: CloudKey<FabricVarpServer> = cloudKey("varp:server")
}
