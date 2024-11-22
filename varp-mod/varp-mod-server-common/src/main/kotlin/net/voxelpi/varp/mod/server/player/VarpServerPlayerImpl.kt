package net.voxelpi.varp.mod.server.player

import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundServerInfoPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundSyncTreePacket
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpPermissions
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

abstract class VarpServerPlayerImpl(
    open val server: VarpServerImpl,
) : VarpServerPlayer {

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

    abstract fun teleport(location: MinecraftLocation): Result<Unit>

    suspend fun createFolder(path: FolderPath, state: FolderState) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.FOLDER_CREATE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Create the folder.
        val folder = server.tree.createFolder(path, state).getOrElse { exception ->
            when (exception) {
                is FolderAlreadyExistsException -> server.messages.sendErrorFolderAlreadyExists(this, exception.path)
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
            return
        }

        // Send confirmation message.
        server.messages.sendFolderCreate(this, folder)
    }

    suspend fun createWarp(path: WarpPath, state: WarpState) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.WARP_CREATE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Create the warp.
        val warp = server.tree.createWarp(path, state).getOrElse { exception ->
            when (exception) {
                is WarpAlreadyExistsException -> server.messages.sendErrorWarpAlreadyExists(this, exception.path)
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
            return
        }

        // Send confirmation message.
        server.messages.sendWarpCreate(this, warp)
    }

    suspend fun deleteFolder(path: FolderPath) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.FOLDER_DELETE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the folder.
        val folder = server.tree.resolve(path)
        if (folder == null) {
            server.messages.sendErrorFolderPathUnresolved(this, path)
            return
        }

        // Temporary save folder data for message.
        val folderPath = folder.path
        val folderState = folder.state

        // Delete the folder.
        folder.delete().onFailure { exception ->
            when (exception) {
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
        }

        // Send confirmation message.
        server.messages.sendFolderDelete(this, folderPath, folderState)
    }

    suspend fun deleteWarp(path: WarpPath) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.WARP_DELETE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the warp.
        val warp = server.tree.resolve(path)
        if (warp == null) {
            server.messages.sendErrorWarpPathUnresolved(this, path)
            return
        }

        // Temporary save warp data for message.
        val warpPath = warp.path
        val warpState = warp.state

        // Delete the warp.
        warp.delete().onFailure { exception ->
            when (exception) {
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
        }

        // Send confirmation message.
        server.messages.sendWarpDelete(this, warpPath, warpState)
    }

    suspend fun moveFolder(src: FolderPath, dst: FolderPath) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.FOLDER_MOVE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the folder.
        val folder = server.tree.resolve(src)
        if (folder == null) {
            server.messages.sendErrorFolderPathUnresolved(this, src)
            return
        }

        // Move the folder.
        folder.move(dst).onFailure { exception ->
            when (exception) {
                is FolderNotFoundException -> server.messages.sendErrorFolderPathUnresolved(this, exception.path)
                is FolderAlreadyExistsException -> server.messages.sendErrorFolderAlreadyExists(this, exception.path)
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
        }

        // Send confirmation message.
        server.messages.sendFolderMove(this, folder, src, dst)
    }

    suspend fun modifyFolder(path: FolderPath, state: FolderState) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.FOLDER_EDIT) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the folder.
        val folder = server.tree.resolve(path)
        if (folder == null) {
            server.messages.sendErrorFolderPathUnresolved(this, path)
            return
        }

        // Edit the folder.
        folder.modify(state).onFailure { exception ->
            when (exception) {
                is FolderNotFoundException -> server.messages.sendErrorFolderPathUnresolved(this, exception.path)
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
        }

        // Send confirmation message.
        server.messages.sendFolderEdit(this, folder)
    }

    suspend fun modifyRoot(state: FolderState) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.ROOT_EDIT) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the root.
        val root = server.tree.root

        // Edit the root.
        root.modify(state).onFailure { exception ->
            when (exception) {
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
        }

        // Send confirmation message.
        server.messages.sendRootEdit(this, root)
    }

    suspend fun moveWarp(src: WarpPath, dst: WarpPath) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.WARP_MOVE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the warp.
        val warp = server.tree.resolve(src)
        if (warp == null) {
            server.messages.sendErrorWarpPathUnresolved(this, src)
            return
        }

        // Move the warp.
        warp.move(dst).onFailure { exception ->
            when (exception) {
                is FolderNotFoundException -> server.messages.sendErrorFolderPathUnresolved(this, exception.path)
                is WarpAlreadyExistsException -> server.messages.sendErrorWarpAlreadyExists(this, exception.path)
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
        }

        // Send confirmation message.
        server.messages.sendWarpMove(this, warp, src, dst)
    }

    suspend fun modifyWarp(path: WarpPath, state: WarpState) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.WARP_EDIT) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the warp.
        val warp = server.tree.resolve(path)
        if (warp == null) {
            server.messages.sendErrorWarpPathUnresolved(this, path)
            return
        }

        // Edit the warp.
        warp.modify(state).onFailure { exception ->
            when (exception) {
                is WarpNotFoundException -> server.messages.sendErrorWarpPathUnresolved(this, exception.path)
                else -> {
                    server.messages.sendErrorGeneric(this)
                    server.logger.error("An unknown error occurred", exception)
                }
            }
        }

        // Send confirmation message.
        server.messages.sendWarpEdit(this, warp)
    }

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
