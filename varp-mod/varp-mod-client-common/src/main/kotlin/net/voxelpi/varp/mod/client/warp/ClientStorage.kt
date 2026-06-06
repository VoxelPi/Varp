package net.voxelpi.varp.mod.client.warp

import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.client.VarpClientImpl
import net.voxelpi.varp.mod.client.network.VarpClientNetworkHandler
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundClientInfoPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyRootStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpStatePacket
import net.voxelpi.varp.repository.Storage
import net.voxelpi.varp.repository.StorageCapability
import net.voxelpi.varp.repository.StorageHandle
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import java.util.EnumSet
import kotlin.reflect.KClass

class ClientStorage(
    private val client: VarpClientImpl,
    private val clientNetworkHandler: VarpClientNetworkHandler,
) : Storage<Unit, StorageHandle> {

    override val capabilities: EnumSet<StorageCapability> = EnumSet.of(
        StorageCapability.RECURSIVE_DELETE,
        StorageCapability.RECURSIVE_MOVE,
    )

    override val configType: KClass<*>
        get() = Unit::class

    override suspend fun open(config: Unit): Result<StorageHandle> {
        TODO("Not yet implemented")
    }

    override suspend fun close(config: Unit, handle: StorageHandle): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun loadContent(config: Unit, handle: StorageHandle): Result<TreeState> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundClientInfoPacket(client.version, VarpModConstants.PROTOCOL_VERSION))
        TODO("Not yet implemented")
    }

    override suspend fun createWarp(config: Unit, handle: StorageHandle, path: WarpPath, state: WarpState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateWarpPacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun createFolder(config: Unit, handle: StorageHandle, path: FolderPath, state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateFolderPacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun saveWarp(config: Unit, handle: StorageHandle, path: WarpPath, state: WarpState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpStatePacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun saveFolder(config: Unit, handle: StorageHandle, path: FolderPath, state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderStatePacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun saveRoot(config: Unit, handle: StorageHandle, state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyRootStatePacket(state))
        return Result.success(Unit)
    }

    override suspend fun deleteWarp(config: Unit, handle: StorageHandle, path: WarpPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteWarpPacket(path))
        return Result.success(Unit)
    }

    override suspend fun deleteFolder(config: Unit, handle: StorageHandle, path: FolderPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteFolderPacket(path))
        return Result.success(Unit)
    }

    override suspend fun moveWarp(config: Unit, handle: StorageHandle, src: WarpPath, dst: WarpPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpPathPacket(src, dst))
        return Result.success(Unit)
    }

    override suspend fun moveFolder(config: Unit, handle: StorageHandle, src: FolderPath, dst: FolderPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderPathPacket(src, dst))
        return Result.success(Unit)
    }
}
