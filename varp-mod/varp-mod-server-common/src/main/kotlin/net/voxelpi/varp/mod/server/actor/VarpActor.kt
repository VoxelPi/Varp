package net.voxelpi.varp.mod.server.actor

import net.kyori.adventure.audience.Audience
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpPermissions
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface VarpActor : Audience {

    val server: VarpServerImpl

    fun hasPermission(permission: String?): Boolean

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

        // Delete the folder.
        deleteFolder(folder)
    }

    suspend fun deleteFolder(folder: Folder) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.FOLDER_DELETE) {
            server.messages.sendErrorNoPermission(this)
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

        // Delete the warp.
        deleteWarp(warp)
    }

    suspend fun deleteWarp(warp: Warp) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.WARP_DELETE) {
            server.messages.sendErrorNoPermission(this)
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
        moveFolder(folder, dst)
    }

    suspend fun moveFolder(folder: Folder, dst: FolderPath) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.FOLDER_MOVE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the previous path.
        val src = folder.path

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
        moveWarp(warp, dst)
    }

    suspend fun moveWarp(warp: Warp, dst: WarpPath) {
        // Check if the player has the required permissions.
        requirePermissionOrElse(VarpPermissions.WARP_MOVE) {
            server.messages.sendErrorNoPermission(this)
            return
        }

        // Get the previous path.
        val src = warp.path

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
}

@OptIn(ExperimentalContracts::class)
inline fun VarpActor.requirePermissionOrElse(permission: String?, onFailure: (String) -> Unit) {
    contract {
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    if (permission != null && !hasPermission(permission)) {
        onFailure.invoke(permission)
    }
}
