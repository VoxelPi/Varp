package net.voxelpi.varp.repository.compositor

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.DuplicatesStrategy
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
import net.voxelpi.varp.exception.tree.NodeParentAlreadyExistsException
import net.voxelpi.varp.exception.tree.NodeParentNotFoundException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.option.DuplicatesStrategyOption
import net.voxelpi.varp.option.MoveMountsOptions
import net.voxelpi.varp.option.OptionsContext
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.repository.RepositoryConfig
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeStateRegistry
import net.voxelpi.varp.tree.state.TreeStateRegistryView
import net.voxelpi.varp.tree.state.WarpState

public class Compositor(
    id: String,
    config: CompositorConfig,
) : Repository(id) {

    override val type: CompositorType
        get() = CompositorType

    override val config: RepositoryConfig
        get() = CompositorConfig(mounts.values.sortedByDescending { it.path.value.length })

    private val mounts: MutableMap<NodeParentPath, CompositorMount> = config.mounts
        .associateBy { it.path }
        .toMutableMap()

    public val eventScope: EventScope = eventScope()

    private val registry: TreeStateRegistry = TreeStateRegistry()

    override val registryView: TreeStateRegistryView
        get() = registry

    init {
        for (mount in this.mounts.values) {
            eventScope.post(CompositorRepositoryMountEvent(this, mount.repository, mount.path))
        }
    }

    override suspend fun load(): Result<Unit> {
        for (mount in this.mounts.values) {
            mount.repository.load().getOrElse { return Result.failure(it) }
        }
        buildTree().onFailure { return Result.failure(it) }
        return Result.success(Unit)
    }

    public fun rebuild(): Result<Unit> {
        return buildTree()
    }

    private fun buildTree(): Result<Unit> {
        registry.clear()

        val mountList = mounts().sortedBy { it.path.value.length }
        for (mount in mountList) {
            val mountPath = mount.path

            // Check that the parent of the mount path exists.
            if (mountPath is FolderPath) {
                if (mountPath.parent !in registry) {
                    registry.clear()
                    throw MissingMountException(mountPath.parent)
                }
            }

            // Copy the root folder of the mount.
            registry[mountPath] = mount.repository.registryView[mount.sourcePath] ?: run {
                registry.clear()
                throw NodeParentNotFoundException(mount.sourcePath)
            }

            // Copy all folders that lie in the repository path.
            for ((path, state) in mount.repository.registryView.folders) {
                // Skip folders that are not children the mounts repository path.
                if (!mount.sourcePath.isTrueSubPathOf(path)) {
                    continue
                }

                // Combine mount location and local path.
                val compositePath = FolderPath(mountPath.value + path.value.substring(mount.sourcePath.value.length))

                // Copy the folder state from the repository tree to the compositor tree.
                registry[compositePath] = state
            }

            // Copy all warps that lie in the repository path.
            for ((path, state) in mount.repository.registryView.warps) {
                // Skip folders that are not children the mounts repository path.
                if (!mount.sourcePath.isTrueSubPathOf(path)) {
                    continue
                }

                // Combine mount location and local path. Ignore duplicate slash in the middle
                val compositePath = WarpPath(mountPath.value + path.value.substring(mount.sourcePath.value.length))

                // Copy the warp state from the repository tree to the compositor tree.
                registry[compositePath] = state
            }
        }

        tree.eventScope.post(RepositoryLoadEvent(this))
        return Result.success(Unit)
    }

    /**
     * Returns all mounts of this compositor.
     */
    public fun mounts(): Collection<CompositorMount> {
        return mounts.values
    }

    /**
     * Returns the mount that is mounted at the given [path].
     * If no repository is mounted there, null is returned.
     */
    public fun mount(path: NodeParentPath): CompositorMount? {
        return mounts[path]
    }

    /**
     * Returns the mount that stores the node at the given [path].
     */
    public fun mountAt(path: NodePath): Result<CompositorMount> {
        if (mounts.isEmpty()) {
            return Result.failure(MissingMountException(path))
        }
        return Result.success(
            mounts.values
                .filter { it.path.isSubPathOf(path) }
                .maxBy { it.path.value.length }
        )
    }

    /**
     * Returns all repositories that are mounted to the compositor.
     * Note that a repository is listed only once, even if it is mounted multiple times.
     */
    public fun mountedRepositories(): Collection<Repository> {
        return mounts.values.map(CompositorMount::repository).associateBy { it.id }.values
    }

    /**
     * Modifies the mount list, by replacing the previous mounts with the mounts present in [newMounts].
     * Note that this loads all mounted repositories.
     */
    public suspend fun modifyMounts(newMounts: Collection<CompositorMount>): Result<Unit> {
        // Remove all mounts.
        val previousMounts = mounts.values.toList()
        mounts.clear()
        registry.clear()

        // Post the unmount event for every old mount.
        for (mount in previousMounts) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount.repository, mount.path))
        }

        // Register all mounts.
        mounts.putAll(newMounts.associateBy { it.path })

        // Rebuild tree
        load()

        // Post the mount event for every new mount.
        for (mount in this.mounts.values) {
            eventScope.post(CompositorRepositoryMountEvent(this, mount.repository, mount.path))
        }

        return Result.success(Unit)
    }

    /**
     * Modifies the mount list using the given [action].
     * Note that this loads all mounted repositories.
     */
    public suspend fun modifyMounts(action: MountModificationContext.() -> Unit): Result<Unit> {
        val context = MountModificationContext(mounts.values)
        context.apply(action)
        return modifyMounts(context.mounts())
    }

    // region repository functions

    override suspend fun create(path: WarpPath, state: WarpState): Result<Unit> {
        // Create warp in mounted repository.
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val repositoryPath = mount.sourcePath / path.relativeTo(mount.path)!!
        mount.repository.create(repositoryPath, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(WarpCreateEvent(tree.resolve(path)!!))

        return Result.success(Unit)
    }

    override suspend fun create(path: FolderPath, state: FolderState): Result<Unit> {
        // Create folder in mounted repository.
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val repositoryPath = mount.sourcePath / path.relativeTo(mount.path)!!
        require(repositoryPath is FolderPath)
        mount.repository.create(repositoryPath, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(FolderCreateEvent(tree.resolve(path)!!))

        return Result.success(Unit)
    }

    override suspend fun save(path: WarpPath, state: WarpState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val repositoryPath = mount.sourcePath / path.relativeTo(mount.path)!!

        // Temporary save previous state.
        val previousState = mount.repository.registryView[repositoryPath] ?: run {
            return Result.failure(WarpNotFoundException(path))
        }

        // Update mounted repository.
        mount.repository.save(repositoryPath, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(WarpStateChangeEvent(tree.resolve(path)!!, state, previousState))

        return Result.success(Unit)
    }

    override suspend fun save(path: FolderPath, state: FolderState): Result<Unit> {
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val repositoryPath = mount.sourcePath / path.relativeTo(mount.path)!!

        // Temporary save previous state.
        val previousState = mount.repository.registryView[repositoryPath] ?: run {
            return Result.failure(FolderNotFoundException(path))
        }

        // Update mounted repository.
        mount.repository.save(repositoryPath, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(FolderStateChangeEvent(tree.resolve(path)!!, state, previousState))

        return Result.success(Unit)
    }

    override suspend fun save(state: FolderState): Result<Unit> {
        val mount = mountAt(RootPath).getOrElse { return Result.failure(it) }
        val repositoryPath = mount.sourcePath

        // Temporary save previous state.
        val previousState = registry.root

        // Update mounted repository.
        mount.repository.save(repositoryPath, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.root = state

        // Post event.
        tree.eventScope.post(RootStateChangeEvent(tree.root, state, previousState))

        return Result.success(Unit)
    }

    override suspend fun delete(path: WarpPath): Result<Unit> {
        val warp = tree.resolve(path) ?: return Result.failure(WarpNotFoundException(path))

        // Post event.
        tree.eventScope.post(WarpDeleteEvent(warp))

        // Update mounted repository.
        val mount = mountAt(path).getOrElse { return Result.failure(it) }
        val repositoryPath = mount.sourcePath / path.relativeTo(mount.path)!!
        mount.repository.delete(repositoryPath).getOrElse { return Result.failure(it) }

        // Update registry.
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
        val repositoryPath = mount.sourcePath / path.relativeTo(mount.path)!!

        // If the mounted root itself is being deleted, content deletion in the repository itself can be skipped.
        if (repositoryPath != mount.sourcePath) {
            when (repositoryPath) {
                is RootPath -> {
                    error("This is impossible")
                }
                is FolderPath -> mount.repository.delete(repositoryPath).getOrElse { return Result.failure(it) }
            }
        }

        // Unmount all mounts that are part of the folder.
        val removedMounts = mounts.values.filter { path.isSubPathOf(it.path) }
        mounts -= removedMounts.map(CompositorMount::path)

        // Remove folder from registry.
        val state = registry.delete(path)

        // Post events.
        if (state != null) {
            tree.eventScope.post(FolderPostDeleteEvent(path, state))
        }
        for (mount in mounts.values) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount.repository, mount.path))
        }

        return Result.success(Unit)
    }

    override suspend fun move(src: WarpPath, dst: WarpPath, options: OptionsContext): Result<Unit> {
        val srcMount = mountAt(src).getOrElse { return Result.failure(it) }
        val dstMount = mountAt(dst).getOrElse { return Result.failure(it) }
        val srcRepositoryPath = srcMount.sourcePath / src.relativeTo(srcMount.path)!!
        val dstRepositoryPath = dstMount.sourcePath / dst.relativeTo(dstMount.path)!!

        if (srcMount.path == dstMount.path) {
            // The warp doesn't change its mount during the move operation.
            val mount = srcMount
            mount.repository.move(srcRepositoryPath, dstRepositoryPath, options).getOrElse { return Result.failure(it) }
        } else {
            // The warp is moved into a different mount.
            val state = registry[src] ?: return Result.failure(WarpNotFoundException(src))
            srcMount.repository.delete(srcRepositoryPath).getOrElse { return Result.failure(it) }
            dstMount.repository.create(dstRepositoryPath, state).getOrElse { return Result.failure(it) }
        }

        // Update registry.
        registry.move(src, dst)

        // Post event.
        tree.eventScope.post(WarpPathChangeEvent(tree.resolve(dst)!!, dst, src))

        return Result.success(Unit)
    }

    override suspend fun move(src: FolderPath, dst: FolderPath, options: OptionsContext): Result<Unit> {
        // Get relevant options.
        val moveMounts = options.getOrDefault(MoveMountsOptions)
        val duplicatesStrategy = options.getOrDefault(DuplicatesStrategyOption)

        // Fail if mounts should be moved, and the destination already exists.
        // This is needed because mounts can't be "merged".
        if (moveMounts && dst in registryView) {
            throw NodeParentAlreadyExistsException(dst)
        }

        // Throw exception when trying to move a folder into one of its children.
        if (src.isTrueSubPathOf(dst)) {
            return Result.failure(FolderMoveIntoChildException(src, dst))
        }

        val srcMount = mountAt(src).getOrElse { return Result.failure(it) }
        val dstMount = mountAt(dst).getOrElse { return Result.failure(it) }
        val srcRepositoryPath = srcMount.sourcePath / src.relativeTo(srcMount.path)!!
        val dstRepositoryPath = dstMount.sourcePath / dst.relativeTo(dstMount.path)!!

        if (moveMounts) {
            // Mounts are moved.
            // This mode always fails if duplicates are detected.

            if (srcMount == dstMount) {
                // Source and destination are the same.
                val mount = srcMount

                // This would mean moving a parent folder into its own child folder, which is not possible as it is already checked.
                require(srcRepositoryPath is FolderPath) { "Can't move root folder" }

                // Perform move in repository.
                mount.repository.move(srcRepositoryPath, dstRepositoryPath, options).getOrElse { return Result.failure(it) }
            } else {
                // Source and destination mount are not the same.

                // Check that the srcRepositoryPath is not the source path of that mount.
                // Would that be the case, the movement is handled only be changing the mounts.
                if (srcRepositoryPath is FolderPath && srcRepositoryPath != srcMount.sourcePath) {
                    // Create a list of all folders that are sub folders (or the folder itself) and sort it by the length of their paths.
                    // That way a parent folder is always created before its child folders, as it has a shorter path.
                    // Then use the list to create all folders at the new location.
                    val relativeFolderPaths = registry.folders.keys.mapNotNull { it.relativeTo(src) }.sortedBy { it.value.length }
                    for (relativePath in relativeFolderPaths) {
                        val state = registry[src / relativePath]!!
                        val folderDst = dst / relativePath
                        // Because move mounts is true, it is ensured that the dst does not exist. Therefore, the dstMount is guaranteed to not change for sub folders.
                        val folderDstRepositoryPath = dstMount.sourcePath / folderDst.relativeTo(dstMount.path)!!
                        when (folderDstRepositoryPath) {
                            is FolderPath -> dstMount.repository.create(folderDstRepositoryPath, state).getOrElse { return Result.failure(it) }
                            RootPath -> error("This is impossible") // dstMount.repository.save(state) // Impossible
                        }
                    }

                    // Create a list of all warps that are children of the moved folder.
                    // Then use the list to create all warps at the new location.
                    val relativeWarpPaths = registry.warps.keys.mapNotNull { it.relativeTo(src) }
                    for (relativePath in relativeWarpPaths) {
                        val state = registry[src / relativePath]!!
                        val warpDst = dst / relativePath
                        // Because move mounts is true, it is ensured that the dst does not exist. Therefore, the dstMount is guaranteed to not change for sub folders.
                        val warpDstRepositoryPath = dstMount.sourcePath / warpDst.relativeTo(dstMount.path)!!
                        dstMount.repository.create(warpDstRepositoryPath, state).getOrElse { return Result.failure(it) }
                    }

                    // Delete folder.
                    srcMount.repository.delete(srcRepositoryPath)
                }
            }

            // Move all mounts that are mounted to the src path or a subpath.
            val mountPaths = mounts.keys.filter { src.isSubPathOf(it) }
            for (srcMountPath in mountPaths) {
                val dstMountPath = dst / srcMountPath.relativeTo(src)!!
                val mount = mounts.remove(srcMountPath)!!
                mounts[dstMountPath] = mount.copy(path = dstMountPath)
            }
        } else {
            // Mounts are not moved, data is always copied between mounts.

            if (srcMount == dstMount && (dst !in registry || mounts.keys.none { dst.isTrueSubPathOf(it) })) {
                // Source mount is the same as the destination mount,
                // and there are mounts inside the destination mount.
                // Therefore, the move operation can be delegated to the mounted repository.

                val mount = srcMount

                // This would mean moving a parent folder into its own child folder, which is not possible as it is already checked.
                require(srcRepositoryPath is FolderPath) { "Can't move root folder" }

                mount.repository.move(srcRepositoryPath, dstRepositoryPath, options).getOrElse { return Result.failure(it) }
            } else {
                // Create a list of all folders that are sub folders (or the folder itself) and sort it by the length of their paths.
                // That way a parent folder is always created before its child folders, as it has a shorter path.
                // Then use the list to create all folders at the new location.
                val affectedFoldersRelativePaths = registry.folders.keys.mapNotNull { it.relativeTo(src) }.sortedBy { it.value.length }
                for (relativePath in affectedFoldersRelativePaths) {
                    val state = registry[src / relativePath]!!
                    val folderDst = dst / relativePath
                    val folderDstMount = mountAt(folderDst).getOrElse { return Result.failure(it) }
                    val folderDstRepositoryPath = folderDstMount.sourcePath / folderDst.relativeTo(folderDstMount.path)!!

                    if (folderDstRepositoryPath in folderDstMount.repository.registryView) {
                        // The destination already exists. Action depends on duplicate strategy.
                        when (duplicatesStrategy) {
                            DuplicatesStrategy.FAIL -> return Result.failure(NodeParentAlreadyExistsException(folderDstRepositoryPath))
                            DuplicatesStrategy.REPLACE_EXISTING -> folderDstMount.repository.save(folderDstRepositoryPath, state).getOrElse { return Result.failure(it) }
                            DuplicatesStrategy.SKIP -> {}
                        }
                    } else {
                        // The destination doesn't already exist and must be created.
                        when (folderDstRepositoryPath) {
                            is FolderPath -> folderDstMount.repository.create(folderDstRepositoryPath, state).getOrElse { return Result.failure(it) }
                            RootPath -> folderDstMount.repository.save(state)
                        }
                    }
                }

                // Create a list of all warps that are children of the moved folder.
                // Then use the list to create all warps at the new location.
                val affectedWarpsRelativePaths = registry.warps.keys.mapNotNull { it.relativeTo(src) }
                for (relativePath in affectedWarpsRelativePaths) {
                    val state = registry[src / relativePath]!!
                    val warpDst = dst / relativePath
                    val warpDstMount = mountAt(warpDst).getOrElse { return Result.failure(it) }
                    val warpDstRepositoryPath = warpDstMount.sourcePath / warpDst.relativeTo(warpDstMount.path)!!

                    if (warpDstRepositoryPath in warpDstMount.repository.registryView) {
                        // The destination already exists. Action depends on duplicate strategy.
                        when (duplicatesStrategy) {
                            DuplicatesStrategy.FAIL -> return Result.failure(WarpAlreadyExistsException(warpDstRepositoryPath))
                            DuplicatesStrategy.REPLACE_EXISTING -> warpDstMount.repository.save(warpDstRepositoryPath, state).getOrElse { return Result.failure(it) }
                            DuplicatesStrategy.SKIP -> {}
                        }
                    } else {
                        // The destination doesn't already exist and must be created.
                        dstMount.repository.create(warpDstRepositoryPath, state).getOrElse { return Result.failure(it) }
                    }
                }

                when (srcRepositoryPath) {
                    is FolderPath -> {
                        // Remove the old folder.
                        srcMount.repository.delete(srcRepositoryPath).getOrElse { return Result.failure(it) }
                    }
                    RootPath -> {
                        // This is handled by removing the mount itself.
                    }
                }

                // Unmount all mounts that are part of the folder.
                val removedMounts = mounts.values.filter { src.isSubPathOf(it.path) }
                mounts -= removedMounts.map(CompositorMount::path)
                for (mount in mounts.values) {
                    eventScope.post(CompositorRepositoryUnmountEvent(this, mount.repository, mount.path))
                }
            }
        }

        // Update registry.
        registry.move(src, dst)

        // Post event.
        tree.eventScope.post(FolderPathChangeEvent(tree.resolve(dst)!!, dst, src))

        return Result.success(Unit)
    }

    override suspend fun move(src: FolderPath, dst: NodeParentPath, options: OptionsContext): Result<Unit> {
        // If mount movement is enabled, check if the destination already exists.
        if (options.getOrDefault(MoveMountsOptions)) {
            if (dst in registryView) {
                return Result.failure(NodeParentAlreadyExistsException(dst))
            }
        }

        return super.move(src, dst, options)
    }

    // endregion

    /**
     * Context used when modifying the mount list.
     */
    public class MountModificationContext(mounts: Collection<CompositorMount>) {
        private val mounts = mounts.associateBy { it.path }.toMutableMap()

        /**
         * Returns all currently registered mounts.
         */
        public fun mounts(): Collection<CompositorMount> {
            return mounts.values
        }

        /**
         * Returns the mount that is mounted at the given [path] or null if no mount is mounted there.
         */
        public fun mount(path: NodeParentPath): CompositorMount? {
            return mounts[path]
        }

        /**
         * Removes all mounts.
         */
        public fun clear() {
            mounts.clear()
        }

        /**
         * Adds a mount for the given [repository] at the given [path].
         * @return The previous mount or null if there was no mount at the given [path].
         */
        public fun register(path: NodeParentPath, repository: Repository, repositoryPath: NodeParentPath): CompositorMount? {
            return mounts.put(path, CompositorMount(path, repository, repositoryPath))
        }

        /**
         * Removes the mount at the given [path].
         * @return The previous mount or null if there was no mount at the given [path].
         */
        public fun unregister(path: NodeParentPath): CompositorMount? {
            return mounts.remove(path)
        }
    }

    public companion object {

        public fun empty(id: String): Compositor {
            return Compositor(id, CompositorConfig.EMPTY)
        }
    }
}
