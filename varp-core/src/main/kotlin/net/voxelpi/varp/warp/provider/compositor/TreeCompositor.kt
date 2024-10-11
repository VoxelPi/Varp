package net.voxelpi.varp.warp.provider.compositor

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.provider.TreeProvider
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.WarpState

public class TreeCompositor internal constructor(
    mounts: Collection<TreeCompositorMount>,
) : TreeProvider {

    private val mounts: MutableMap<NodeParentPath, TreeCompositorMount> = mounts
        .sortedByDescending { it.location.value.length }
        .associateBy { it.location }
        .toMutableMap()

    override val registry: TreeStateRegistry = TreeStateRegistry()

    init {
        // Check if all locations are unique
        require(mounts.size == mounts.map(TreeCompositorMount::location).size) { "Duplicate mounts detected." }

        // Check that root mount is present.
        val rootMount = mounts.last() // Get mount with shorted location. Should be the root path.
        require(rootMount.location == RootPath) { "No storage mounted in root path." }

        for (mount in mounts.reversed()) {
            val location = mount.location
            when (location) {
                is RootPath -> {
                    registry.root = mount.provider.registry.root
                }
                is FolderPath -> {
                    registry[location] = mount.provider.registry.root
                }
            }

            for ((path, state) in mount.provider.registry.folders) {
                // Combine mount location and local path. Ignore duplicate slash in the middle
                val compositePath = FolderPath(location.value + path.value.substring(1))

                registry[compositePath] = state
            }
        }
    }

    public fun mounts(): Collection<TreeCompositorMount> {
        return mounts.values
    }

    public fun mountAt(path: NodePath): TreeCompositorMount {
        return mounts.values
            .filter { it.location.isTrueSubPathOf(path) }
            .maxBy { it.location.value.length }
    }

    override fun createWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        mount.provider.createWarpState(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun createFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        require(relativePath is FolderPath)
        mount.provider.createFolderState(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        mount.provider.saveWarpState(relativePath, state).getOrElse { return Result.failure(it) }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        when (relativePath) {
            is RootPath -> mount.provider.saveRootState(state).getOrElse { return Result.failure(it) }
            is FolderPath -> mount.provider.saveFolderState(path, state).getOrElse { return Result.failure(it) }
        }

        registry[path] = state

        return Result.success(Unit)
    }

    override fun saveRootState(state: FolderState): Result<Unit> {
        val mount = mountAt(RootPath)
        mount.provider.saveRootState(state).getOrElse { return Result.failure(it) }

        registry.root = state

        return Result.success(Unit)
    }

    override fun deleteWarpState(path: WarpPath): Result<Unit> {
        val mount = mountAt(path)
        val relativePath = path.relativeTo(mount.location)!!
        mount.provider.deleteWarpState(relativePath).getOrElse { return Result.failure(it) }

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
            is FolderPath -> mount.provider.deleteFolderState(path).getOrElse { return Result.failure(it) }
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
            mount.provider.moveWarpState(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
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
//            mount.provider.moveFolderState(srcRelativePath, dstRelativePath).getOrElse { return Result.failure(it) }
        } else {
            TODO("MOVEMENT BETWEEN MOUNTS NOT YET IMPLEMENTED")
            // This probably should just change the mount location.
        }

        registry.move(src, dst)

        return Result.success(Unit)
    }
}
