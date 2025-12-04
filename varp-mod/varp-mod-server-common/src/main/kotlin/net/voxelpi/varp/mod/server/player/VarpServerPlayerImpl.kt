package net.voxelpi.varp.mod.server.player

import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundServerInfoPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundSyncTreePacket
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.actor.VarpActor
import net.voxelpi.varp.mod.server.actor.requirePermissionOrElse
import net.voxelpi.varp.mod.server.api.VarpPermissions
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import net.voxelpi.varp.warp.path.WarpPath

abstract class VarpServerPlayerImpl(
    override val server: VarpServerImpl,
) : VarpServerPlayer, VarpActor {

    override var clientInformation: VarpClientInformation? = null
        protected set

    fun enableBridge(clientInformation: VarpClientInformation) {
        // Check if protocol versions are compatible.
        if (VarpModConstants.PROTOCOL_VERSION != clientInformation.protocolVersion) {
            server.messages.sendClientErrorIncompatibleProtocolVersion(this, clientInformation, server.info)
            return
        }

        // Enable bridge if it is not already enabled.
        if (this.clientInformation != clientInformation) {
            this.clientInformation = clientInformation
            if (server.platform.isDedicated) {
                server.messages.sendClientSupportEnabled(this, clientInformation)
            }
        }

        // TODO: Maybe delay by 2 ticks?
        // Send server info packet.
        server.serverNetworkHandler.sendClientboundPacket(VarpClientboundServerInfoPacket(server.info), this)

        // Send sync packet.
        server.serverNetworkHandler.sendClientboundPacket(VarpClientboundSyncTreePacket(server.tree), this)
    }

    abstract fun teleport(location: MinecraftLocation): Result<Unit>

    fun teleportToWarp(path: WarpPath) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.WARP_TELEPORT_SELF) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the warp.
        val warp = server.tree.resolve(path)
        if (warp == null) {
            server.messages.sendErrorWarpPathUnresolved(this, path)
            return
        }

        // Teleport the player to the warp.
        teleport(warp.state.location)

        // Send confirmation message.
        server.messages.sendWarpTeleportSelf(this, warp)
    }
}
