package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.event.compositor.CompositorRepositoryMountEvent
import net.voxelpi.varp.event.compositor.CompositorRepositoryUnmountEvent
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
import net.voxelpi.varp.warp.state.WarpState
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@RepositoryType("compositor")
public class Compositor internal constructor(
    override val id: String,
    mounts: Collection<CompositorMount>,
) : Repository {

    private val mounts: MutableMap<NodeParentPath, CompositorMount> = mounts
        .sortedByDescending { it.location.value.length }
        .associateBy { it.location }
        .toMutableMap()

    public val eventScope: EventScope = eventScope()

    override val registryView: TreeStateRegistry = TreeStateRegistry()

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
        registryView.clear()

        // Check if all locations are unique
        require(mounts.size == mounts.values.map(CompositorMount::location).size) { "Duplicate mounts detected." }

        val mountList = mounts().sortedByDescending { it.location.value.length }
        for (mount in mountList) {
            val location = mount.location
            when (location) {
                is RootPath -> {
                    registryView.root = mount.repository.registryView.root
                }
                is FolderPath -> {
                    registryView[location] = mount.repository.registryView.root
                }
            }

            for ((path, state) in mount.repository.registryView.folders) {
                // Combine mount location and local path. Ignore duplicate slash in the middle
                val compositePath = FolderPath(location.value + path.value.substring(1))

                registryView[compositePath] = state
            }
        }
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
                .filter { it.location.isTrueSubPathOf(path) }
                .maxBy { it.location.value.length }
        )
    }

    override suspend fun create(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!
        mount.repository.create(relativePath, state).getOrElse { return Result.failure(it) }

        registryView[path] = state

        return Result.success(Unit)
    }

    override suspend fun create(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!
        require(relativePath is FolderPath)
        mount.repository.create(relativePath, state).getOrElse { return Result.failure(it) }

        registryView[path] = state

        return Result.success(Unit)
    }

    override suspend fun save(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!
        mount.repository.save(relativePath, state).getOrElse { return Result.failure(it) }

        registryView[path] = state

        return Result.success(Unit)
    }

    override suspend fun save(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!
        when (relativePath) {
            is RootPath -> mount.repository.save(state).getOrElse { return Result.failure(it) }
            is FolderPath -> mount.repository.save(path, state).getOrElse { return Result.failure(it) }
        }

        registryView[path] = state

        return Result.success(Unit)
    }

    override suspend fun save(state: FolderState): Result<Unit> {
        val mount = mountAt(RootPath).getOrElse { return Result.failure(it) }
        mount.repository.save(state).getOrElse { return Result.failure(it) }

        registryView.root = state

        return Result.success(Unit)
    }

    override suspend fun delete(path: WarpPath): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!
        mount.repository.delete(relativePath).getOrElse { return Result.failure(it) }

        registryView.delete(path)

        return Result.success(Unit)
    }

    override suspend fun delete(path: FolderPath): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val relativePath = path.relativeTo(mount.location)!!

        when (relativePath) {
            is RootPath -> {
                // This is handled by removing the mount itself.
                // TODO: Should this also clear the mounted repository?
            }
            is FolderPath -> mount.repository.delete(path).getOrElse { return Result.failure(it) }
        }

        // Unmount all mounts that are part of the folder.
        val removedMounts = mounts.values.filter { path.isSubPathOf(it.location) }
        mounts -= removedMounts.map(CompositorMount::location)

        // TODO: Recursive remove from registry.
        registryView.delete(path)

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
            val mount = srcMount
            mount.repository.move(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
        } else {
            TODO("MOVEMENT BETWEEN MOUNTS NOT YET IMPLEMENTED")
            // This probably should just change the mount location.
        }

        registryView.move(src, dst)

        return Result.success(Unit)
    }

    override suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit> {
        val srcMount = mountAt(src).getOrElse { return Result.failure(it) }
        val dstMount = mountAt(dst).getOrElse { return Result.failure(it) }
        val srcRelativePath = src.relativeTo(srcMount.location)!!
        val dstRelativePath = dst.relativeTo(dstMount.location)!!

        if (srcMount.location == dstMount.location) {
            val mount = srcMount
            if (dstRelativePath !is FolderPath || srcRelativePath !is FolderPath) {
                // What happens if the folder is moved to the root of the mount?
                TODO("NOT IMPLEMENTED")
                return Result.success(Unit)
            }
            mount.repository.move(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
        } else {
            TODO("MOVEMENT BETWEEN MOUNTS NOT YET IMPLEMENTED")
            // This probably should just change the mount location.
        }

        registryView.move(src, dst)

        return Result.success(Unit)
    }

    public companion object {

        public fun empty(id: String): Compositor {
            return Compositor(id, emptyList())
        }
    }
}
