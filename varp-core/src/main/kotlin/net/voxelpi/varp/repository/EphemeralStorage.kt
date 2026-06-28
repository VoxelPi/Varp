package net.voxelpi.varp.repository

import net.kyori.adventure.key.Key
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import kotlin.reflect.KClass

/**
 * A repository storage that doesn't actually store the repository data.
 */
public object EphemeralStorage : Storage<Unit, StorageHandle> {

    override val id: Key = Key.key("varp", "ephemeral")

    override val configType: KClass<Unit>
        get() = Unit::class

    override suspend fun open(config: Unit, defaultState: TreeState): Result<StorageHandle> = runCatching { StorageHandle.Simple(defaultState) }

    override suspend fun close(config: Unit, handle: StorageHandle): Result<Unit> = runCatching {}

    override suspend fun loadTree(config: Unit, handle: StorageHandle): Result<TreeState> = runCatching { handle.defaultState }

    override suspend fun updateTree(config: Unit, handle: StorageHandle, path: NodeParentPath, state: TreeState): Result<Unit> = runCatching {}

    override suspend fun createWarp(config: Unit, handle: StorageHandle, path: WarpPath, state: WarpState): Result<Unit> = runCatching {}

    override suspend fun createFolder(config: Unit, handle: StorageHandle, path: FolderPath, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun updateWarp(config: Unit, handle: StorageHandle, path: WarpPath, state: WarpState): Result<Unit> = runCatching {}

    override suspend fun updateFolder(config: Unit, handle: StorageHandle, path: FolderPath, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun updateRoot(config: Unit, handle: StorageHandle, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun deleteWarp(config: Unit, handle: StorageHandle, path: WarpPath): Result<Unit> = runCatching {}

    override suspend fun deleteFolder(config: Unit, handle: StorageHandle, path: FolderPath): Result<Unit> = runCatching {}

    override suspend fun moveWarp(config: Unit, handle: StorageHandle, src: WarpPath, dst: WarpPath): Result<Unit> = runCatching {}

    override suspend fun moveFolder(config: Unit, handle: StorageHandle, src: FolderPath, dst: FolderPath): Result<Unit> = runCatching {}
}
