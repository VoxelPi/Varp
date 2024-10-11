package net.voxelpi.varp.warp

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.api.event.folder.FolderCreateEvent
import net.voxelpi.varp.api.event.folder.FolderDeleteEvent
import net.voxelpi.varp.api.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.api.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.api.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.api.event.root.RootStateChangeEvent
import net.voxelpi.varp.api.event.warp.WarpCreateEvent
import net.voxelpi.varp.api.event.warp.WarpDeleteEvent
import net.voxelpi.varp.api.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.api.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.api.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.api.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.api.exception.tree.FolderMoveIntoChildException
import net.voxelpi.varp.api.exception.tree.FolderNotFoundException
import net.voxelpi.varp.api.exception.tree.NodeParentNotFoundException
import net.voxelpi.varp.api.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.api.exception.tree.WarpNotFoundException
import net.voxelpi.varp.api.warp.Folder
import net.voxelpi.varp.api.warp.Tree
import net.voxelpi.varp.api.warp.Warp
import net.voxelpi.varp.api.warp.node.NodeParent
import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.NodeChildPath
import net.voxelpi.varp.api.warp.path.NodeParentPath
import net.voxelpi.varp.api.warp.path.NodePath
import net.voxelpi.varp.api.warp.path.RootPath
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.provider.TreeProvider
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.state.RootState
import net.voxelpi.varp.api.warp.state.WarpState
import net.voxelpi.varp.warp.tree.VarpNodeParent

class VarpTree(
    override val provider: TreeProvider,
) : Tree {

    val eventScope: EventScope = eventScope()

    override val root: VarpRoot = VarpRoot(this)

    override fun warp(path: WarpPath): VarpWarp? {
        if (!exists(path)) {
            return null
        }
        return VarpWarp(this, path)
    }

    override fun folder(path: FolderPath): VarpFolder? {
        if (!exists(path)) {
            return null
        }
        return VarpFolder(this, path)
    }

    override fun container(path: NodeParentPath): VarpNodeParent? {
        return when (path) {
            is RootPath -> root
            is FolderPath -> folder(path)
        }
    }

    override fun warpState(path: WarpPath): WarpState? {
        return provider.registry.warps[path]
    }

    override fun warpState(path: WarpPath, state: WarpState): Result<Unit> {
        // Check if a warp exists at the given path.
        val previousState = warpState(path) ?: run {
            return Result.failure(WarpNotFoundException(path))
        }

        // Save the warp state at the given path.
        provider.saveWarpState(path, state).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(WarpStateChangeEvent(VarpWarp(this, path), state, previousState))

        return Result.success(Unit)
    }

    override fun folderState(path: FolderPath): FolderState? {
        return provider.registry.folders[path]
    }

    override fun folderState(path: FolderPath, state: FolderState): Result<Unit> {
        // Check if a folder exists at the given path.
        val previousState = folderState(path) ?: run {
            return Result.failure(FolderNotFoundException(path))
        }

        // Save the folder state at the given path.
        provider.saveFolderState(path, state).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(FolderStateChangeEvent(VarpFolder(this, path), state, previousState))

        return Result.success(Unit)
    }

    override fun rootState(): RootState {
        return provider.registry.root
    }

    override fun rootState(state: RootState): Result<Unit> {
        // Check if a module exists at the given path.
        val previousState = rootState()

        // Save the module state at the given path.
        provider.saveRootState(state).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(RootStateChangeEvent(VarpRoot(this), state, previousState))

        return Result.success(Unit)
    }

    override fun warps(): Collection<VarpWarp> {
        return provider.registry.warps.keys.map { path -> VarpWarp(this, path) }
    }

    override fun folders(): Collection<VarpFolder> {
        return provider.registry.folders.keys.map { path -> VarpFolder(this, path) }
    }

    override fun containers(): Collection<VarpNodeParent> {
        return folders() + listOf(root)
    }

    override fun exists(path: WarpPath): Boolean {
        return provider.registry.warps.contains(path)
    }

    override fun exists(path: FolderPath): Boolean {
        return provider.registry.folders.contains(path)
    }

    override fun exists(path: NodeParentPath): Boolean {
        return when (path) {
            is RootPath -> true
            is FolderPath -> exists(path)
        }
    }

    override fun exists(path: NodeChildPath): Boolean {
        return when (path) {
            is FolderPath -> exists(path)
            is WarpPath -> exists(path)
        }
    }

    override fun exists(path: NodePath): Boolean {
        return when (path) {
            is RootPath -> true
            is FolderPath -> exists(path)
            is WarpPath -> exists(path)
        }
    }

    override fun createWarp(path: WarpPath, state: WarpState): Result<VarpWarp> {
        // Check if the warp already exists.
        if (exists(path)) {
            return Result.failure(WarpAlreadyExistsException(path))
        }

        // Check if the parent exists.
        if (!exists(path.parent)) {
            return Result.failure(NodeParentNotFoundException(path.parent))
        }

        // Create the warp state.
        provider.createWarpState(path, state).onFailure { return Result.failure(it) }
        val warp = VarpWarp(this, path)

        // Post event.
        eventScope.post(WarpCreateEvent(warp))

        return Result.success(warp)
    }

    override fun createFolder(path: FolderPath, state: FolderState): Result<VarpFolder> {
        // Check if the folder already exists.
        if (exists(path)) {
            return Result.failure(FolderAlreadyExistsException(path))
        }

        // Check if the parent exists.
        if (!exists(path.parent)) {
            return Result.failure(NodeParentNotFoundException(path.parent))
        }

        // Save the folder state.
        provider.createFolderState(path, state).onFailure { return Result.failure(it) }
        val folder = VarpFolder(this, path)

        // Post event.
        eventScope.post(FolderCreateEvent(folder))

        return Result.success(folder)
    }

    override fun deleteWarp(path: WarpPath): Result<Unit> {
        // Check if the warp exists.
        val state = warpState(path) ?: return Result.failure(WarpNotFoundException(path))

        // Post event.
        eventScope.post(WarpDeleteEvent(VarpWarp(this, path)))

        // Delete the warp state.
        provider.deleteWarpState(path)

        // Post event.
        eventScope.post(WarpPostDeleteEvent(path, state))
        return Result.success(Unit)
    }

    override fun deleteFolder(path: FolderPath): Result<Unit> {
        // Check if the folder exists.
        val state = folderState(path) ?: return Result.failure(FolderNotFoundException(path))

        // Post event.
        eventScope.post(FolderDeleteEvent(VarpFolder(this, path)))

        // Delete the folder state.
        provider.deleteFolderState(path)

        // Post event.
        eventScope.post(FolderPostDeleteEvent(path, state))
        return Result.success(Unit)
    }

    override fun warps(path: NodeParentPath, recursive: Boolean): Collection<VarpWarp> {
        return if (recursive) {
            warps().filter { path.contains(it.path) }
        } else {
            warps().filter { it.path.parent == path }
        }
    }

    override fun folders(path: NodeParentPath, recursive: Boolean): Collection<VarpFolder> {
        return if (recursive) {
            folders().filter { path.contains(it.path) }
        } else {
            folders().filter { it.path.parent == path }
        }
    }

    override fun warps(path: NodeParentPath, recursive: Boolean, predicate: (Warp) -> Boolean): Collection<VarpWarp> {
        return warps(path, recursive).filter(predicate)
    }

    override fun folders(path: NodeParentPath, recursive: Boolean, predicate: (Folder) -> Boolean): Collection<VarpFolder> {
        return folders(path, recursive).filter(predicate)
    }

    override fun warps(predicate: (Warp) -> Boolean): Collection<VarpWarp> {
        return warps().filter(predicate)
    }

    override fun folders(predicate: (Folder) -> Boolean): Collection<VarpFolder> {
        return folders().filter(predicate)
    }

    override fun containers(predicate: (NodeParent) -> Boolean): Collection<VarpNodeParent> {
        return containers().filter(predicate)
    }

    override fun move(src: WarpPath, dst: WarpPath): Result<Unit> {
        // Early return if source and destination path are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Fail if a warp already exists at the destination path.
        if (exists(dst)) {
            return Result.failure(WarpAlreadyExistsException(dst))
        }

        // Move the state.
        provider.moveWarpState(src, dst).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(WarpPathChangeEvent(VarpWarp(this, dst), dst, src))

        return Result.success(Unit)
    }

    override fun move(src: FolderPath, dst: FolderPath): Result<Unit> {
        // Early return if source and destination path are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Throw exception when trying to move a folder into one of its children.
        if (src.contains(dst)) {
            return Result.failure(FolderMoveIntoChildException(src, dst))
        }

        // Fail if a folder already exists at the destination path.
        if (exists(dst)) {
            return Result.failure(FolderAlreadyExistsException(dst))
        }

        // Move the state.
        provider.moveFolderState(src, dst).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(FolderPathChangeEvent(VarpFolder(this, dst), dst, src))

        return Result.success(Unit)
    }
}
