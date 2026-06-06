package net.voxelpi.varp.repository

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import java.util.EnumSet
import kotlin.reflect.KClass

/**
 * A repository storage that doesn't actually store the repository data.
 */
public object EphemeralStorage : Storage<Unit, StorageHandle> {

    override val capabilities: EnumSet<StorageCapability> = EnumSet.of(
        StorageCapability.RECURSIVE_DELETE,
        StorageCapability.RECURSIVE_MOVE,
    )

    override val configType: KClass<*>
        get() = Unit::class

    override suspend fun open(config: Unit): Result<StorageHandle> = runCatching { StorageHandle.Simple() }

    override suspend fun close(config: Unit, handle: StorageHandle): Result<Unit> = runCatching {}

    override suspend fun loadContent(config: Unit, handle: StorageHandle): Result<TreeState> = runCatching { MutableTreeState() }

    override suspend fun createWarp(config: Unit, handle: StorageHandle, path: WarpPath, state: WarpState): Result<Unit> = runCatching {}

    override suspend fun createFolder(config: Unit, handle: StorageHandle, path: FolderPath, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun saveWarp(config: Unit, handle: StorageHandle, path: WarpPath, state: WarpState): Result<Unit> = runCatching {}

    override suspend fun saveFolder(config: Unit, handle: StorageHandle, path: FolderPath, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun saveRoot(config: Unit, handle: StorageHandle, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun deleteWarp(config: Unit, handle: StorageHandle, path: WarpPath): Result<Unit> = runCatching {}

    override suspend fun deleteFolder(config: Unit, handle: StorageHandle, path: FolderPath): Result<Unit> = runCatching {}

    override suspend fun moveWarp(config: Unit, handle: StorageHandle, src: WarpPath, dst: WarpPath): Result<Unit> = runCatching {}

    override suspend fun moveFolder(config: Unit, handle: StorageHandle, src: FolderPath, dst: FolderPath): Result<Unit> = runCatching {}
}
