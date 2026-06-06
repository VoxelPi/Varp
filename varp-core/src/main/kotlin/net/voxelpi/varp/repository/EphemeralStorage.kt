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
public object EphemeralStorage : Storage<Unit, Unit> {

    override val capabilities: EnumSet<StorageCapability> = EnumSet.of(
        StorageCapability.RECURSIVE_DELETE,
        StorageCapability.RECURSIVE_MOVE,
    )

    override val configType: KClass<*>
        get() = Unit::class

    override suspend fun open(config: Unit): Result<Unit> = runCatching {}

    override suspend fun close(config: Unit, handle: Unit): Result<Unit> = runCatching {}

    override suspend fun loadContent(config: Unit, handle: Unit): Result<TreeState> = runCatching { MutableTreeState() }

    override suspend fun createWarp(config: Unit, handle: Unit, path: WarpPath, state: WarpState): Result<Unit> = runCatching {}

    override suspend fun createFolder(config: Unit, handle: Unit, path: FolderPath, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun saveWarp(config: Unit, handle: Unit, path: WarpPath, state: WarpState): Result<Unit> = runCatching {}

    override suspend fun saveFolder(config: Unit, handle: Unit, path: FolderPath, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun saveRoot(config: Unit, handle: Unit, state: FolderState): Result<Unit> = runCatching {}

    override suspend fun deleteWarp(config: Unit, handle: Unit, path: WarpPath): Result<Unit> = runCatching {}

    override suspend fun deleteFolder(config: Unit, handle: Unit, path: FolderPath): Result<Unit> = runCatching {}

    override suspend fun moveWarp(config: Unit, handle: Unit, src: WarpPath, dst: WarpPath): Result<Unit> = runCatching {}

    override suspend fun moveFolder(config: Unit, handle: Unit, src: FolderPath, dst: FolderPath): Result<Unit> = runCatching {}
}
