package net.voxelpi.varp.repository

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import java.util.EnumSet
import kotlin.reflect.KClass

public interface Storage<C : Any, H : StorageHandle> {

    public val capabilities: EnumSet<StorageCapability>

    public val configType: KClass<C>

    public suspend fun open(config: C): Result<H>

    public suspend fun close(config: C, handle: H): Result<Unit>

    public suspend fun loadContent(config: C, handle: H): Result<TreeState>

    public suspend fun createWarp(config: C, handle: H, path: WarpPath, state: WarpState): Result<Unit>

    public suspend fun createFolder(config: C, handle: H, path: FolderPath, state: FolderState): Result<Unit>

    public suspend fun saveWarp(config: C, handle: H, path: WarpPath, state: WarpState): Result<Unit>

    public suspend fun saveFolder(config: C, handle: H, path: FolderPath, state: FolderState): Result<Unit>

    public suspend fun saveRoot(config: C, handle: H, state: FolderState): Result<Unit>

    public suspend fun deleteWarp(config: C, handle: H, path: WarpPath): Result<Unit>

    public suspend fun deleteFolder(config: C, handle: H, path: FolderPath): Result<Unit>

    public suspend fun moveWarp(config: C, handle: H, src: WarpPath, dst: WarpPath): Result<Unit>

    public suspend fun moveFolder(config: C, handle: H, src: FolderPath, dst: FolderPath): Result<Unit>

    public fun createInstance(config: C): StorageInstance<C, H> {
        return StorageInstance(this, config)
    }
}
