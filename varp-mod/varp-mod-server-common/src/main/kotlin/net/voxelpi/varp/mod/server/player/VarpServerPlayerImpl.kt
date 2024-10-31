package net.voxelpi.varp.mod.server.player

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.player.ServersideClientInformation
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

    fun enableClientSupport(clientInformation: ServersideClientInformation) {
        TODO()
    }

    suspend fun createFolder(path: FolderPath, state: FolderState): Result<Unit> {
        TODO()
    }

    suspend fun createWarp(path: WarpPath, state: WarpState): Result<Unit> {
        TODO()
    }

    suspend fun deleteFolder(path: FolderPath): Result<Unit> {
        TODO()
    }

    suspend fun deleteWarp(path: WarpPath): Result<Unit> {
        TODO()
    }

    suspend fun moveFolder(src: FolderPath, dst: FolderPath): Result<Unit> {
        TODO()
    }

    suspend fun modifyFolder(path: FolderPath, state: FolderState): Result<Unit> {
        TODO()
    }

    suspend fun modifyRoot(state: FolderState): Result<Unit> {
        TODO()
    }

    suspend fun moveWarp(src: WarpPath, dst: WarpPath): Result<Unit> {
        TODO()
    }

    suspend fun modifyWarp(path: WarpPath, state: WarpState): Result<Unit> {
        TODO()
    }

    fun teleportToWarp(path: WarpPath): Result<Unit> {
        TODO()
    }
}
