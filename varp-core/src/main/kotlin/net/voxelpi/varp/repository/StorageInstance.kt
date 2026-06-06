package net.voxelpi.varp.repository

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import java.util.EnumSet

@JvmRecord
public data class StorageInstance<C : Any, H : Any>(
    public val storage: Storage<C, H>,
    public val config: C,
) {

    public val capabilities: EnumSet<StorageCapability>
        get() = storage.capabilities

    public suspend fun open(): Result<H> {
        return storage.open(config)
    }

    public suspend fun close(handle: H): Result<Unit> {
        return storage.close(config, handle)
    }

    public suspend fun loadContent(handle: H): Result<TreeState> {
        return storage.loadContent(config, handle)
    }

    public suspend fun createWarp(handle: H, path: WarpPath, state: WarpState): Result<Unit> {
        return storage.createWarp(config, handle, path, state)
    }

    public suspend fun createFolder(handle: H, path: FolderPath, state: FolderState): Result<Unit> {
        return storage.createFolder(config, handle, path, state)
    }

    public suspend fun saveWarp(handle: H, path: WarpPath, state: WarpState): Result<Unit> {
        return storage.saveWarp(config, handle, path, state)
    }

    public suspend fun saveFolder(handle: H, path: FolderPath, state: FolderState): Result<Unit> {
        return storage.saveFolder(config, handle, path, state)
    }

    public suspend fun saveRoot(handle: H, state: FolderState): Result<Unit> {
        return storage.saveRoot(config, handle, state)
    }

    public suspend fun deleteWarp(handle: H, path: WarpPath): Result<Unit> {
        return storage.deleteWarp(config, handle, path)
    }

    public suspend fun deleteFolder(handle: H, path: FolderPath): Result<Unit> {
        return storage.deleteFolder(config, handle, path)
    }

    public suspend fun moveWarp(handle: H, src: WarpPath, dst: WarpPath): Result<Unit> {
        return storage.moveWarp(config, handle, src, dst)
    }

    public suspend fun moveFolder(handle: H, src: FolderPath, dst: FolderPath): Result<Unit> {
        return storage.moveFolder(config, handle, src, dst)
    }
}
