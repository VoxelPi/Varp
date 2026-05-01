package net.voxelpi.varp.mod.paper.command

import net.voxelpi.varp.mod.paper.PaperVarpServer
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.kotlin.extension.cloudKey

object PaperVarpCommandArguments {

    val SERVER: CloudKey<PaperVarpServer> = cloudKey("varp:server")
}
