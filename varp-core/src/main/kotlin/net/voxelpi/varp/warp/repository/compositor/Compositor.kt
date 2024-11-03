package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.event.compositor.CompositorRepositoryMountEvent
import net.voxelpi.varp.event.compositor.CompositorRepositoryUnmountEvent
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.repository.RepositoryLoadEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.exception.tree.FolderMoveIntoChildException
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.RepositoryLoader
import net.voxelpi.varp.warp.repository.RepositoryType
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@RepositoryType("compositor")
public class Compositor internal constructor(
    id: String,
    mounts: Collection<CompositorMount>,
) : Repository(id) {

    private val mounts: MutableMap<NodeParentPath, CompositorMount> = mounts
        .sortedByDescending { it.location.value.length }
        .associateBy { it.location }
        .toMutableMap()

    public val eventScope: EventScope = eventScope()

    private val registry: TreeStateRegistry = TreeStateRegistry()

    override val registryView: TreeStateRegistryView
        get() = registry

    init {
        buildTree()
        for (mount in this.mounts.values) {
            eventScope.post(CompositorRepositoryMountEvent(this, mount.repository, mount.location))
        }
    }

    @RepositoryLoader
    public constructor(id: String, config: CompositorConfig) : this(id, config.mounts) {}

    override suspend fun load(): Result<Unit> {
        for (mount in this.mounts.values) {
            mount.repository.load().getOrElse { return Result.failure(it) }
        }
        buildTree()
        return Result.success(Unit)
    }

    private fun buildTree() {
        registry.clear()

        // Check if all locations are unique
        require(mounts.size == mounts.values.map(CompositorMount::location).size) { "Duplicate mounts detected." }

        val mountList = mounts().sortedByDescending { it.location.value.length }
        for (mount in mountList) {
            val location = mount.location
            when (location) {
                is RootPath -> {
                    registry.root = mount.repository.registryView.root
                }
                is FolderPath -> {
                    registry[location] = mount.repository.registryView.root
                }
            }

            for ((path, state) in mount.repository.registryView.folders) {
                // Combine mount location and local path. Ignore duplicate slash in the middle
                val compositePath = FolderPath(location.value + path.value.substring(1))

                registry[compositePath] = state
            }

            for ((path, state) in mount.repository.registryView.warps) {
                // Combine mount location and local path. Ignore duplicate slash in the middle
                val compositePath = WarpPath(location.value + path.value.substring(1))

                registry[compositePath] = state
            }
        }

        tree.eventScope.post(RepositoryLoadEvent(this))
    }

    public fun mounts(): Collection<CompositorMount> {
        return mounts.values
    }

    public fun addMount(path: NodeParentPath, repository: Repository) {
        mounts[path] = CompositorMount(path, repository)
        buildTree()
        eventScope.post(CompositorRepositoryMountEvent(this, repository, path))
    }

    public fun removeMount(path: NodeParentPath) {
        val mount = mounts.remove(path) ?: return
        buildTree()
        eventScope.post(CompositorRepositoryUnmountEvent(this, mount.repository, mount.location))
    }

    public fun clearMounts() {
        val previousMounts = mounts.values.toList()
        mounts.clear()
        buildTree()
        for (mount in previousMounts) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount.repository, mount.location))
        }
    }

    public fun updateMounts(mounts: Collection<CompositorMount>) {
        clearMounts()

        this.mounts.putAll(mounts.associateBy { it.location })
        buildTree()
        for (mount in this.mounts.values) {
            eventScope.post(CompositorRepositoryMountEvent(this, mount.repository, mount.location))
        }
    }

    public fun mountAt(path: NodePath): Result<CompositorMount> {
        if (mounts.isEmpty()) {
            return Result.failure(NoMountException(path))
        }
        return Result.success(
            mounts.values
                .filter { it.location.isSubPathOf(path) }
                .maxBy { it.location.value.length }
        )
    }

    override suspend fun create(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!
        mount.repository.create(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        // Post event.
        tree.eventScope.post(WarpCreateEvent(tree.resolve(path)!!))

        return Result.success(Unit)
    }

    override suspend fun create(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!
        require(relativePath is FolderPath)
        mount.repository.create(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        // Post event.
        tree.eventScope.post(FolderCreateEvent(tree.resolve(path)!!))

        return Result.success(Unit)
    }

    override suspend fun save(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!

        // Temporary save previous state.
        val previousState = mount.repository.registryView[relativePath] ?: run {
            return Result.failure(WarpNotFoundException(path))
        }

        // Update mounted repository.
        mount.repository.save(relativePath, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(WarpStateChangeEvent(tree.resolve(path)!!, state, previousState))

        return Result.success(Unit)
    }

    override suspend fun save(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!

        // Temporary save previous state.
        val previousState = mount.repository.registryView[relativePath] ?: run {
            return Result.failure(FolderNotFoundException(path))
        }

        // Update mounted repository.
        when (relativePath) {
            is RootPath -> mount.repository.save(state).getOrElse { return Result.failure(it) }
            is FolderPath -> mount.repository.save(path, state).getOrElse { return Result.failure(it) }
        }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(FolderStateChangeEvent(tree.resolve(path)!!, state, previousState))

        return Result.success(Unit)
    }

    override suspend fun save(state: FolderState): Result<Unit> {
        // Temporary save previous state.
        val previousState = registry.root

        val mount = mountAt(RootPath).getOrElse { return Result.failure(it) }
        mount.repository.save(state).getOrElse { return Result.failure(it) }

        registry.root = state

        // Post event.
        tree.eventScope.post(RootStateChangeEvent(tree.root, state, previousState))

        return Result.success(Unit)
    }

    override suspend fun delete(path: WarpPath): Result<Unit> {
        val warp = tree.resolve(path) ?: return Result.failure(WarpNotFoundException(path))

        // Post event.
        tree.eventScope.post(WarpDeleteEvent(warp))

        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!

        mount.repository.delete(relativePath).getOrElse { return Result.failure(it) }

        val state = registry.delete(path)

        // Post event.
        if (state != null) {
            tree.eventScope.post(WarpPostDeleteEvent(path, state))
        }

        return Result.success(Unit)
    }

    override suspend fun delete(path: FolderPath): Result<Unit> {
        val folder = tree.resolve(path) ?: return Result.failure(FolderNotFoundException(path))

        // Post event.
        tree.eventScope.post(FolderDeleteEvent(folder))

        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!

        when (relativePath) {
            is RootPath -> {
                // This is handled by removing the mount itself.
            }
            is FolderPath -> mount.repository.delete(relativePath).getOrElse { return Result.failure(it) }
        }

        // Unmount all mounts that are part of the folder.
        val removedMounts = mounts.values.filter { path.isSubPathOf(it.location) }
        mounts -= removedMounts.map(CompositorMount::location)

        // Remove folder from registry.
        val state = registry.delete(path)

        // Post event.
        if (state != null) {
            tree.eventScope.post(FolderPostDeleteEvent(path, state))
        }

        for (mount in mounts.values) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount.repository, mount.location))
        }

        return Result.success(Unit)
    }

    override suspend fun move(src: WarpPath, dst: WarpPath): Result<Unit> {
        val srcMount = mountAt(src).getOrElse { return Result.failure(it) }
        val dstMount = mountAt(dst).getOrElse { return Result.failure(it) }
        val srcRelativePath = src.relativeTo(srcMount.location)!!
        val dstRelativePath = dst.relativeTo(dstMount.location)!!

        if (srcMount.location == dstMount.location) {
            // The warp doesn't change its mount during the move operation.
            val mount = srcMount
            mount.repository.move(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
        } else {
            // The warp is moved into a different mount.
            val state = registry[src] ?: return Result.failure(WarpNotFoundException(src))
            srcMount.repository.delete(srcRelativePath).getOrElse { return Result.failure(it) }
            dstMount.repository.create(dstRelativePath, state).getOrElse { return Result.failure(it) }
        }

        // Update registry.
        registry.move(src, dst)

        // Post event.
        tree.eventScope.post(WarpPathChangeEvent(tree.resolve(dst)!!, dst, src))

        return Result.success(Unit)
    }

    override suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit> {
        val srcMount = mountAt(src).getOrElse { return Result.failure(it) }
        val dstMount = mountAt(dst).getOrElse { return Result.failure(it) }
        val srcRelativePath = src.relativeTo(srcMount.location)!!
        val dstRelativePath = dst.relativeTo(dstMount.location)!!

        if (srcMount.location == dstMount.location) {
            val mount = srcMount
            when (dstRelativePath) {
                is FolderPath -> {
                    if (srcRelativePath !is FolderPath) {
                        return Result.failure(FolderMoveIntoChildException(src, dst))
                    }
                    mount.repository.move(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
                }
                RootPath -> {
                    mount.repository.save(registry[src]!!)

                    // Move all direct child folders
                    val directChildFolders = mount.repository.registryView.folders.keys
                        .filter { srcRelativePath.isTrueSubPathOf(it) && !it.value.substring(srcRelativePath.value.length, it.value.length - 1).contains("/") }
                    for (folder in directChildFolders) {
                        mount.repository.move(folder, folder.relativeTo(srcRelativePath)!! as FolderPath).getOrElse { return Result.failure(it) }
                    }

                    // Move all direct child warps.
                    val directChildWarps = mount.repository.registryView.warps.keys
                        .filter { srcRelativePath.isSubPathOf(it) && !it.value.substring(srcRelativePath.value.length).contains("/") }
                    for (warp in directChildWarps) {
                        mount.repository.move(warp, warp.relativeTo(srcRelativePath)!!).getOrElse { return Result.failure(it) }
                    }
                }
            }
        } else {
            // Create a list of all folders that are sub folders (or the folder itself) and sort it by the length of their paths.
            // That way a parent folder is always created before its child folders, as it has a shorter path.
            // Then use the list to create all folders at the new location.
            val affectedFoldersRelativePaths = registry.folders.keys.filter(src::isSubPathOf).sortedBy { it.value.length }
            for (oldPath in affectedFoldersRelativePaths) {
                val state = registry[oldPath]!!
                val newPath = FolderPath(dst.value + oldPath.relativeTo(src)!!.value.substring(1)).relativeTo(dstMount.location)!!
                when (newPath) {
                    is FolderPath -> dstMount.repository.create(newPath, state).getOrElse { return Result.failure(it) }
                    RootPath -> dstMount.repository.save(state)
                }
            }

            // Create a list of all warps that are children of the moved folder.
            // Then use the list to create all warps at the new location.
            val affectedWarpsRelativePaths = registry.warps.keys.filter(src::isSubPathOf)
            for (oldPath in affectedWarpsRelativePaths) {
                val state = registry[oldPath]!!
                val newPath = WarpPath(dst.value + oldPath.relativeTo(src)!!.value.substring(1)).relativeTo(dstMount.location)!!
                dstMount.repository.create(newPath, state).getOrElse { return Result.failure(it) }
            }

            when (srcRelativePath) {
                is FolderPath -> {
                    // Remove the old folder.
                    srcMount.repository.delete(srcRelativePath).getOrElse { return Result.failure(it) }
                }
                RootPath -> {
                    // This is handled by removing the mount itself.
                }
            }

            // Unmount all mounts that are part of the folder.
            val removedMounts = mounts.values.filter { src.isSubPathOf(it.location) }
            mounts -= removedMounts.map(CompositorMount::location)

            for (mount in mounts.values) {
                eventScope.post(CompositorRepositoryUnmountEvent(this, mount.repository, mount.location))
            }
        }

        // Update registry.
        registry.move(src, dst)

        // Post event.
        tree.eventScope.post(FolderPathChangeEvent(tree.resolve(dst)!!, dst, src))

        return Result.success(Unit)
    }

    public companion object {

        public fun empty(id: String): Compositor {
            return Compositor(id, emptyList())
        }
    }
}
