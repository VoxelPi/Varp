package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.RepositoryLoader
import net.voxelpi.varp.warp.repository.RepositoryType
import net.voxelpi.varp.warp.repository.ephemeral.EphemeralRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.WarpState
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@RepositoryType("compositor")
public class TreeCompositor internal constructor(
    override val id: String,
    mounts: Collection<TreeCompositorMount>,
) : Repository {

    private val mounts: MutableMap<NodeParentPath, TreeCompositorMount> = mounts
        .sortedByDescending { it.location.value.length }
        .associateBy { it.location }
        .toMutableMap()

    override val registry: TreeStateRegistry = TreeStateRegistry()

    init {
        buildTree()
    }

    @RepositoryLoader
    public constructor(id: String, config: TreeCompositorConfig) : this(id, config.mounts) {}

    override fun reload(): Result<Unit> {
        registry.clear()
        buildTree()
        return Result.success(Unit)
    }

    private fun buildTree() {
        // Check if all locations are unique
        require(mounts.size == mounts.values.map(TreeCompositorMount::location).size) { "Duplicate mounts detected." }

        // Check that root mount is present.
        val rootMount = mounts[RootPath] // Get mount with shorted location. Should be the root path.
        require(rootMount != null) { "No repository mounted in the root path." }

        val mountList = mounts().sortedByDescending { it.location.value.length }
        for (mount in mountList) {
            val location = mount.location
            when (location) {
                is RootPath -> {
                    registry.root = mount.repository.registry.root
                }
                is FolderPath -> {
                    registry[location] = mount.repository.registry.root
                }
            }

            for ((path, state) in mount.repository.registry.folders) {
                // Combine mount location and local path. Ignore duplicate slash in the middle
                val compositePath = FolderPath(location.value + path.value.substring(1))

                registry[compositePath] = state
            }
        }
    }

    public fun mounts(): Collection<TreeCompositorMount> {
        return mounts.values
    }

    public fun addMount(path: NodeParentPath, repository: Repository) {
        mounts[path] = TreeCompositorMount(path, repository)
        buildTree()
    }

    public fun removeMount(path: NodeParentPath) {
        mounts.remove(path)
        buildTree()
    }

    public fun clearMounts() {
        mounts.clear()
        mounts[RootPath] = TreeCompositorMount(RootPath, EphemeralRepository("default"))
        buildTree()
    }

    public fun updateMounts(mounts: Collection<TreeCompositorMount>) {
        this.mounts.clear()
        this.mounts.putAll(mounts.associateBy { it.location })
        buildTree()
    }

    public fun mountAt(path: NodePath): TreeCompositorMount {
        return mounts.values
            .filter { it.location.isTrueSubPathOf(path) }
            .maxBy { it.location.value.length }
    }

    override fun createWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        mount.repository.createWarpState(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun createFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        require(relativePath is FolderPath)
        mount.repository.createFolderState(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        mount.repository.saveWarpState(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        when (relativePath) {
            is RootPath -> mount.repository.saveRootState(state).getOrElse { return Result.failure(it) }
            is FolderPath -> mount.repository.saveFolderState(path, state).getOrElse { return Result.failure(it) }
        }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun saveRootState(state: FolderState): Result<Unit> {
        val mount = mountAt(RootPath)
        mount.repository.saveRootState(state).getOrElse { return Result.failure(it) }

        registry.root = state

        return Result.success(Unit)
    }

    override fun deleteWarpState(path: WarpPath): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        mount.repository.deleteWarpState(relativePath).getOrElse { return Result.failure(it) }

        registry.remove(path)

        return Result.success(Unit)
    }

    override fun deleteFolderState(path: FolderPath): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        when (relativePath) {
            is RootPath -> {
                TODO("REMOVING MOUNT DIR NOT YET IMPLEMENTED")
                // This probably should just also remove the mount itself. (And clear the mounted storage)
            }
            is FolderPath -> mount.repository.deleteFolderState(path).getOrElse { return Result.failure(it) }
        }

        registry.remove(path)

        return Result.success(Unit)
    }

    override fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit> {
        val srcMount = mountAt(src)
        val dstMount = mountAt(dst)
        val srcRelativePath = src.relativeTo(srcMount.location)!!
        val dstRelativePath = dst.relativeTo(dstMount.location)!!

        if (srcMount.location == dstMount.location) {
            val mount = srcMount
            mount.repository.moveWarpState(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
        } else {
            TODO("MOVEMENT BETWEEN MOUNTS NOT YET IMPLEMENTED")
            // This probably should just change the mount location.
        }

        registry.move(src, dst)

        return Result.success(Unit)
    }

    override fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit> {
        val srcMount = mountAt(src)
        val dstMount = mountAt(dst)
        val srcRelativePath = src.relativeTo(srcMount.location)!!
        val dstRelativePath = dst.relativeTo(dstMount.location)!!

        if (srcMount.location == dstMount.location) {
            val mount = srcMount
            TODO("NOT IMPLEMENTED")
            // What happens if the folder is moved to the root of the mount?
//            mount.repository.moveFolderState(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
        } else {
            TODO("MOVEMENT BETWEEN MOUNTS NOT YET IMPLEMENTED")
            // This probably should just change the mount location.
        }

        registry.move(src, dst)

        return Result.success(Unit)
    }

    public companion object {

        public fun simple(id: String, repository: Repository): TreeCompositor {
            return TreeCompositor(
                id,
                listOf(
                    TreeCompositorMount(RootPath, repository)
                ),
            )
        }
    }
}
