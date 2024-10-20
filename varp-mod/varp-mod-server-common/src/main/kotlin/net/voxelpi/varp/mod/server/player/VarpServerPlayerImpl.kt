package net.voxelpi.varp.mod.server.player

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.player.ServersideClientInformation
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

abstract class VarpServerPlayerImpl(
    open val server: VarpServerImpl,
) : VarpServerPlayer {

    override var clientInformation: ServersideClientInformation? = null
        protected set

    abstract fun hasPermission(permission: String?): Boolean

    @OptIn(ExperimentalContracts::class)
    inline fun requirePermissionOrElse(permission: String?, onFailure: (String) -> Unit) {
        contract {
            callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
        }
        if (permission != null && !hasPermission(permission)) {
            onFailure.invoke(permission)
        }
    }
}
