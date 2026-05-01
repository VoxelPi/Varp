package net.voxelpi.varp.mod.server.command

import kotlinx.coroutines.CoroutineScope
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.message.VarpMessages
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.kotlin.extension.cloudKey

object VarpModCommandArguments {

    val SERVER: CloudKey<VarpServerImpl> = cloudKey("varp:server")

    val MESSAGE_SERVICE: CloudKey<VarpMessages> = cloudKey("varp:messages")

    val COROUTINE_SCOPE: CloudKey<CoroutineScope> = cloudKey("varp:coroutine_scope")
}
