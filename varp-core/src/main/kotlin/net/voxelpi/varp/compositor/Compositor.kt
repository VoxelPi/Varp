package net.voxelpi.varp.compositor

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.on
import net.voxelpi.event.post
import net.voxelpi.varp.event.compositor.CompositorRepositoryMountEvent
import net.voxelpi.varp.event.compositor.CompositorRepositoryUnmountEvent
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.node.NodeParentStateChangeEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.tree.TreeUpdateEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.exception.tree.FolderMoveIntoChildException
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.NodeParentAlreadyExistsException
import net.voxelpi.varp.exception.tree.NodeParentNotFoundException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.Warp
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.path.topLevelPaths
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import net.voxelpi.varp.util.Movement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class Compositor(
    mounts: List<CompositorMount>,
) : Tree {

    private val mounts: MutableMap<NodeParentPath, CompositorMount> = mounts
        .associateBy { it.targetPath }
        .toMutableMap()

    public override val eventScope: EventScope = eventScope()

    override val state: TreeState
        field = MutableTreeState()

    private val repositoriesEventScope: EventScope = eventScope()
    private val subscribedRepositories: MutableSet<Repository<*, *>> = mutableSetOf()

    // Caches for nodes that are moved between repositories.
    private val crossRepositoryWarpMoves: MutableSet<Movement<WarpPath>> = mutableSetOf()
    private val crossRepositoryFolderMoves: MutableSet<Movement<FolderPath>> = mutableSetOf()

    init {
        for (mount in this.mounts.values) {
            eventScope.post(CompositorRepositoryMountEvent(this, mount))
            addMountToEventBus(mount)
        }

        // Handle any event in a mounted repository.
        // This is also how events are created when performing actions via the compositor.
        repositoriesEventScope.on { event: TreeUpdateEvent ->
            val updatedRepository = event.tree as? Repository<*, *> ?: return@on

            val previousState = state.subtree(event.path)
            val mounts = mounts().filter { it.repository == event.tree }
            // TODO: Handle internally.

            // eventScope.post(TreeUpdateEvent(this, previousState, state))
        }
        repositoriesEventScope.on { event: WarpCreateEvent ->
            // We need to check for each mount point of the repository if the warp should be present in the compositor tree.
            // If yes, we add it to the compositor tree and call the warp create event.
            for (mount in mounts().filter { it.repository == event.warp.tree }) {
                val compositorPath = toCompositorPath(mount, event.warp.path) ?: run {
                    // The warp is not present in the compositor tree.
                    continue
                }
                state[compositorPath] = event.warp.state

                val crossRepositoryMove = crossRepositoryWarpMoves.find { it.to == compositorPath }
                if (crossRepositoryMove != null) {
                    // The creation of the warp was caused by a cross-repository compositor move.
                    eventScope.post(WarpPathChangeEvent(this[compositorPath]!!, newPath = crossRepositoryMove.to, oldPath = crossRepositoryMove.from))
                } else {
                    // Post a create event in the compositor event scope.
                    eventScope.post(WarpCreateEvent(this[compositorPath]!!))
                }
            }
        }
        repositoriesEventScope.on { event: FolderCreateEvent ->
            // We need to check for each mount point of the repository if the folder should be present in the compositor tree.
            // If yes, we add it to the compositor tree and call the folder create event.
            for (mount in mounts().filter { it.repository == event.folder.tree }) {
                val compositorPath = toCompositorPath(mount, event.folder.path) ?: run {
                    // The folder is not present in the compositor tree.
                    continue
                }
                when (compositorPath) {
                    is FolderPath -> {
                        state[compositorPath] = event.folder.state

                        val crossRepositoryMove = crossRepositoryFolderMoves.find { it.to == compositorPath }
                        if (crossRepositoryMove != null) {
                            // The creation of the folder was caused by a cross-repository compositor move.
                            eventScope.post(FolderPathChangeEvent(this[compositorPath]!!, newPath = crossRepositoryMove.to, oldPath = crossRepositoryMove.from))
                        } else {
                            // Post a create event in the compositor event scope.
                            eventScope.post(FolderCreateEvent(this[compositorPath]!!))
                        }
                    }
                    RootPath -> {
                        // This shouldn't be possible, as the mount source folder must exist when loading the tree.
                        throw NodeParentAlreadyExistsException(RootPath)
                    }
                }
            }
        }
        repositoriesEventScope.on { event: WarpDeleteEvent ->
            // We need to check for each mount point of the repository if the warp is present in the compositor tree.
            // If yes, we remove it from the compositor tree and call the warp delete event.
            for (mount in mounts().filter { it.repository == event.warp.tree }) {
                val compositorPath = toCompositorPath(mount, event.warp.path) ?: run {
                    // The warp is not present in the compositor tree.
                    continue
                }

                val crossRepositoryMove = crossRepositoryWarpMoves.find { it.from == compositorPath }
                if (crossRepositoryMove != null) {
                    eventScope.post(WarpDeleteEvent(this[compositorPath]!!))
                }
                val previousState = state.delete(compositorPath)!!
                if (crossRepositoryMove != null) {
                    eventScope.post(WarpPostDeleteEvent(compositorPath, previousState))
                }
            }
        }
        repositoriesEventScope.on { event: FolderDeleteEvent ->
            // TODO: Handle cross-mount move.

            // Collect all representatives of the deleted folder in the compositor tree.
            val compositorPaths = mounts()
                .filter { it.repository == event.folder.tree }
                .mapNotNull { toCompositorPath(it, event.folder.path) }

            // Collect all mounts that need to be removed because they have a source path that is a proper subpath of the deleted node.
            val removedMounts = mounts()
                .filter { it.repository == event.folder.tree }
                .filter { it.sourcePath.isProperSubpathOf(event.folder.path) }
                .associateBy { it.targetPath }

            // Find top level nodes from the union of the above two sets.
            val removedCompositorFolders = topLevelPaths(compositorPaths + removedMounts.keys)

            // Handle the case where the root node should be "removed".
            if (RootPath in removedCompositorFolders) {
                // Unmount everything.
                for (mount in mounts().sortedByDescending { it.targetPath.value.length }) {
                    eventScope.post(CompositorRepositoryUnmountEvent(this, mount))
                }
                this.mounts.clear()

                // Fire a delete event for each warp directly in the root.
                for (warp in warps(RootPath, recursive = false)) {
                    eventScope.post(WarpDeleteEvent(this[warp.path]!!))
                    val previousState = this.state.delete(warp.path)!!
                    eventScope.post(WarpPostDeleteEvent(warp.path, previousState))
                }
                // Fire a delete event for each folder directly in the root.
                for (folder in folders(RootPath, recursive = false)) {
                    eventScope.post(FolderDeleteEvent(this[folder.path]!!))
                    val previousState = this.state.delete(folder.path)!!
                    eventScope.post(FolderPostDeleteEvent(folder.path, previousState))
                }
                // Reset the root node.
                val previousRootState = this.state.root
                this.state.clear()
                eventScope.post(RootStateChangeEvent(this.root, this.state.root, previousRootState))

                return@on
            }

            // We can cast to folder path, now that we now that the root path is not present.
            @Suppress("UNCHECKED_CAST")
            removedCompositorFolders as Set<FolderPath>
            @Suppress("UNCHECKED_CAST")
            removedMounts as MutableMap<FolderPath, CompositorMount>

            // Unmount all mounts that were previously selected.
            for (mount in removedMounts.values.sortedByDescending { it.targetPath.value.length }) {
                eventScope.post(CompositorRepositoryUnmountEvent(this, mount))
                this.mounts -= mount.targetPath
            }

            // Delete all folders that were previously selected.
            for (compositorPath in removedCompositorFolders) {
                eventScope.post(FolderDeleteEvent(this[compositorPath]!!))
                val previousState = state.delete(compositorPath)!!
                eventScope.post(FolderPostDeleteEvent(compositorPath, previousState))
            }
        }
        repositoriesEventScope.on { event: WarpStateChangeEvent ->
            // We need to check for each mount point of the repository if the warp is present in the compositor tree.
            // If yes, we update it in the compositor tree and call the warp state change event.
            for (mount in mounts().filter { it.repository == event.warp.tree }) {
                val compositorPath = toCompositorPath(mount, event.warp.path) ?: run {
                    // The warp is not present in the compositor tree.
                    continue
                }
                state[compositorPath] = event.warp.state
                eventScope.post(WarpStateChangeEvent(this[compositorPath]!!, event.oldState, event.newState))
            }
        }
        repositoriesEventScope.on { event: NodeParentStateChangeEvent ->
            // We need to check for each mount point of the repository if the folder is present in the compositor tree.
            // If yes, we update it in the compositor tree and call the warp state change event.
            for (mount in mounts().filter { it.repository == event.node.tree }) {
                val compositorPath = toCompositorPath(mount, event.node.path) ?: run {
                    // The warp is not present in the compositor tree.
                    continue
                }

                if (compositorPath == mount.targetPath) {
                    // The state is provided by the mount point and not the repository, therefore it doesn't change.
                    continue
                }

                // Update state.
                state[compositorPath] = event.newState

                // Fire event.
                when (compositorPath) {
                    is FolderPath -> eventScope.post(FolderStateChangeEvent(this[compositorPath]!!, newState = event.newState, oldState = event.oldState))
                    RootPath -> eventScope.post(RootStateChangeEvent(this[RootPath], newState = event.newState, oldState = event.oldState))
                }
            }
        }
        repositoriesEventScope.on { event: WarpPathChangeEvent ->
            // We need to check for each mount point of the repository if the warp is present in the compositor tree.
            // If yes, we update it in the compositor tree and call the event for the compositor warp.
            for (mount in mounts().filter { it.repository == event.warp.tree }) {
                val oldCompositorPath = toCompositorPath(mount, event.oldPath)
                val newCompositorPath = toCompositorPath(mount, event.newPath)

                // Depending on if the old and new paths are present in the compositor tree, different actions must be performed.
                if (oldCompositorPath != null) {
                    if (newCompositorPath != null) {
                        // Both the old and the new path are present in the compositor tree,
                        // we therefore just move the state in the compositor tree.
                        state.move(oldCompositorPath, newCompositorPath)
                        eventScope.post(WarpPathChangeEvent(this[newCompositorPath]!!, newCompositorPath, oldCompositorPath))
                    } else {
                        // Whilst the warp was previously present in the compositor tree,
                        // it was moved into a location that is no longer present in the compositor tree.
                        // We therefore delete the warp from the compositor tree.
                        eventScope.post(WarpDeleteEvent(this[oldCompositorPath]!!))
                        val previousState = state.delete(oldCompositorPath)!!
                        eventScope.post(WarpPostDeleteEvent(oldCompositorPath, previousState))
                    }
                } else {
                    if (newCompositorPath != null) {
                        // Whilst the warp was previously not present in the compositor tree,
                        // it was moved into a location that is now present in the compositor tree.
                        // We therefore create the warp in the compositor tree.
                        state[newCompositorPath] = event.warp.state
                        eventScope.post(WarpCreateEvent(this[newCompositorPath]!!))
                    } else {
                        // The warp remain not present in the compositor tree. Nothing to do here.
                    }
                }
            }
        }
        repositoriesEventScope.on { event: FolderPathChangeEvent ->
            // TODO: Handle internally.
            // TODO: The new path could be affected by a mount.

            eventScope.post(FolderPathChangeEvent(this[event.folder.path]!!, event.newPath, event.oldPath))
        }
    }

    /**
     * Clears the state of the compositor.
     * This means that all mounts are removed, and the tree state is cleared.
     */
    public fun clear() {
        // Remove all mounts.
        for (mount in mounts()) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount))
        }
        mounts.clear()

        // Clear the compositor tree state.
        state.clear()
    }

    public suspend fun load(): Result<Unit> {
        if (RootPath !in this.mounts) {
            throw MissingMountException(RootPath)
        }

        val repositories = mounts.values.map { it.repository }.toSet()
        for (repository in repositories) {
            repository.load().getOrElse { return Result.failure(it) }
        }
        buildTree().onFailure { return Result.failure(it) }
        return Result.success(Unit)
    }

    public fun rebuild(): Result<Unit> {
        return buildTree()
    }

    private fun buildTree(): Result<Unit> = runCatching {
        val previousTreeState = state.copy()
        state.clear()

        if (mounts.isEmpty()) {
            throw MissingMountException(RootPath)
        }

        val mountList = mounts().sortedBy { it.targetPath.value.length }
        for (mount in mountList) {
            val mountPath = mount.targetPath

            // Check that the parent of the mount path exists.
            if (mountPath is FolderPath) {
                if (mountPath.parent !in state) {
                    state.clear()
                    throw NodeParentNotFoundException(mountPath.parent)
                }
            }

            // Copy the root folder of the mount.
            if (mount.sourcePath !in mount.repository) {
                state.clear()
                throw NodeParentNotFoundException(mount.sourcePath)
            }
            state[mountPath] = mount.state

            // Copy all folders that are children of the mounts source container.
            for ((repositoryPath, folderState) in mount.repository.state.folders) {
                // Skip folders that are not children the mounts repository path.
                // Also skip the mount source, as it is already handled above.
                if (!repositoryPath.isProperSubpathOf(mount.sourcePath)) {
                    continue
                }

                // Get the path where the node should be placed in the compositor tree.
                val compositorPath = toCompositorPath(mount, repositoryPath) ?: run {
                    // The given repositoryPath is not represented by this mount in the compositor.
                    // This can be because it is not a subpath of the source path, or if there is a more specific mount that shadows this mount.
                    continue
                }

                // Copy the folder state from the repository tree to the compositor tree.
                this.state[compositorPath] = folderState
            }

            // Copy all warps that are children of the mounts source container.
            for ((repositoryPath, warpState) in mount.repository.state.warps) {
                // Skip warps that are not children the mounts repository path.
                if (!repositoryPath.isProperSubpathOf(mount.sourcePath)) {
                    continue
                }

                // Get the path where the node should be placed in the compositor tree.
                val compositorPath = toCompositorPath(mount, repositoryPath) ?: run {
                    // The given repositoryPath is not represented by this mount in the compositor.
                    // This can be because it is not a subpath of the source path, or if there is a more specific mount that shadows this mount.
                    continue
                }

                // Copy the warp state from the repository tree to the compositor tree.
                this.state[compositorPath] = warpState
            }
        }

        eventScope.post(TreeUpdateEvent(this, RootPath, previousTreeState, state))
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
    public fun mountFor(path: NodePath): CompositorMount {
        if (mounts.isEmpty()) {
            throw MissingMountException(RootPath)
        }
        return mounts.values
            .filter { path.isSubpathOf(it.targetPath) }
            .maxBy { it.targetPath.value.length }
    }

    /**
     * Returns the mount that stores the node at the given [path].
     */
    public fun mountForOrNull(path: NodePath): CompositorMount? {
        return mounts.values
            .filter { path.isSubpathOf(it.targetPath) }
            .maxByOrNull { it.targetPath.value.length }
    }

    /**
     * Resolves [compositorPath] to the repository location that provides its content.
     */
    public fun toRepositoryLocation(compositorPath: NodePath): Pair<CompositorMount, NodePath> {
        val mount = mountFor(compositorPath)
        val repositoryPath = mount.sourcePath / compositorPath.relativeTo(mount.targetPath)!!
        return Pair(mount, repositoryPath)
    }

    /**
     * Resolves [compositorPath] to the repository location that provides its content.
     */
    public fun toRepositoryLocation(compositorPath: NodeParentPath): Pair<CompositorMount, NodeParentPath> {
        val mount = mountFor(compositorPath)
        val repositoryPath = mount.sourcePath / compositorPath.relativeTo(mount.targetPath)!!
        return Pair(mount, repositoryPath)
    }

    /**
     * Resolves [compositorPath] to the repository location that provides its content.
     */
    public fun toRepositoryLocation(compositorPath: WarpPath): Pair<CompositorMount, WarpPath> {
        val mount = mountFor(compositorPath)
        val repositoryPath = mount.sourcePath / compositorPath.relativeTo(mount.targetPath)!!
        return Pair(mount, repositoryPath)
    }

    /**
     * Returns the path at which [repositoryPath] is visible in the composed tree.
     *
     * Returns `null` if the path is not exposed by the mount or if it is hidden by
     * a more specific mount.
     */
    public fun toCompositorPath(mount: CompositorMount, repositoryPath: NodePath): NodePath? {
        val relativePath = repositoryPath.relativeTo(mount.sourcePath) ?: return null
        val compositorPath = mount.targetPath / relativePath

        // Check if the path is overridden by another compositor.
        if (mounts().any { it.targetPath.isProperSubpathOf(mount.targetPath) && compositorPath.isSubpathOf(it.targetPath) }) {
            return null
        }
        return compositorPath
    }

    /**
     * Returns the path at which [repositoryPath] is visible in the composed tree.
     *
     * Returns `null` if the path is not exposed by the mount or if it is hidden by
     * a more specific mount.
     */
    public fun toCompositorPath(mount: CompositorMount, repositoryPath: NodeParentPath): NodeParentPath? {
        val relativePath = repositoryPath.relativeTo(mount.sourcePath) ?: return null
        val compositorPath = mount.targetPath / relativePath

        // Check if the path is overridden by another compositor.
        if (mounts().any { it.targetPath.isProperSubpathOf(mount.targetPath) && compositorPath.isSubpathOf(it.targetPath) }) {
            return null
        }
        return compositorPath
    }

    /**
     * Returns the path at which [repositoryPath] is visible in the composed tree.
     *
     * Returns `null` if the path is not exposed by the mount or if it is hidden by
     * a more specific mount.
     */
    public fun toCompositorPath(mount: CompositorMount, repositoryPath: WarpPath): WarpPath? {
        val relativePath = repositoryPath.relativeTo(mount.sourcePath) ?: return null
        val compositorPath = mount.targetPath / relativePath

        // Check if the path is overridden by another compositor.
        if (mounts().any { it.targetPath.isProperSubpathOf(mount.targetPath) && compositorPath.isSubpathOf(it.targetPath) }) {
            return null
        }
        return compositorPath
    }

    /**
     * Returns all repositories that are mounted to the compositor.
     * Note that a repository is listed only once, even if it is mounted multiple times.
     */
    public fun mountedRepositories(): Collection<Repository<*, *>> {
        return mounts.values.map(CompositorMount::repository).toSet()
    }

    /**
     * Modifies the mount list, by replacing the previous mounts with the mounts present in [newMounts].
     * Note that this loads all mounted repositories.
     */
    public suspend fun modifyMounts(newMounts: Collection<CompositorMount>): Result<Unit> {
        // Remove all mounts.
        val previousMounts = mounts.values.toList()
        mounts.clear()
        state.clear()

        // Post the unmount event for every old mount.
        for (mount in previousMounts) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount))
        }

        // Register all mounts.
        mounts.putAll(newMounts.associateBy { it.targetPath })
        for (newMount in newMounts) {
            addMountToEventBus(newMount)
        }

        // Rebuild tree
        load().onFailure { return Result.failure(it) }

        // Post the mount event for every new mount.
        for (mount in this.mounts.values) {
            eventScope.post(CompositorRepositoryMountEvent(this, mount))
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

    private fun addMountToEventBus(mount: CompositorMount) {
        val repository = mount.repository
        if (repository !in subscribedRepositories) {
            subscribedRepositories += repository
            repository.eventScope.register(repositoriesEventScope)
        }
    }

    private fun removeMountFromEventBus(mount: CompositorMount) {
        val repository = mount.repository
        if (repository in subscribedRepositories && mounts.values.none { it.repository == repository }) {
            subscribedRepositories -= repository
            repository.eventScope.unregister(repositoriesEventScope)
        }
    }

    /**
     * Removes a mount from the compositor and updates the tree.
     * This also removes all mounts that targeted a subpath of the mount.
     * Also adds nodes that were previously shadowed by the mount back to the tree.
     */
    private fun removeMount(mount: CompositorMount): Result<Unit> = runCatching {
        // Remove all mounts that target a subpath of the mount.
        // This includes the mount itself.
        mounts()
            .filter { it.targetPath.isSubpathOf(it.targetPath) }
            .sortedByDescending { it.targetPath.value.length }
            .forEach { subMount ->
                eventScope.post(CompositorRepositoryUnmountEvent(this, mount))
                mounts -= subMount.targetPath
            }

        // Handle the case when the root mount was removed.
        if (mount.targetPath !is FolderPath) {
            // Target path is the root path, the compositor tree is therefore now empty.
            state.clear()
            return@runCatching
        }

        // Update the compositor tree state.
        state.delete(mount.targetPath)

        // Add nodes that were previously shadowed by the mount.
        // TODO: Should they cause create events?
        val (parentMount, parentRepositoryPath) = toRepositoryLocation(mount.targetPath)
        state.folders += parentMount.repository.state.folders
            .filter { it.key.isSubpathOf(parentRepositoryPath) } // We already handled the case where the compositor path was the root path and can therefore cast to folder path.
            .mapKeys { toCompositorPath(parentMount, it.key)!! as FolderPath } // Null-safe, because there are no sub-mounts that could shadow it.
        state.warps += parentMount.repository.state.warps
            .filter { it.key.isSubpathOf(parentRepositoryPath) }
            .mapKeys { toCompositorPath(parentMount, it.key)!! } // Null-safe, because there are no sub-mounts that could shadow it.
    }

    private fun removeMounts(mounts: Collection<CompositorMount>, additionalDeletedPaths: Set<NodeParentPath>): Result<Unit> = runCatching {
        val mounts = mounts.associateBy { it.targetPath }
        val topLevelPaths = topLevelPaths(mounts.keys)
    }

    // region repository functions

    override suspend fun create(path: WarpPath, state: WarpState): Result<Warp> = runCatching {
        if (path in this) {
            throw WarpAlreadyExistsException(path)
        }
        if (path.parent !in this) {
            throw NodeParentNotFoundException(path.parent)
        }

        // Create the warp in the mounted repository.
        // The compositor state is then updated by the event handler for that repository.
        val (mount, repositoryPath) = toRepositoryLocation(path)
        mount.repository.create(repositoryPath, state).getOrThrow()
        return@runCatching Warp(this, path)
    }

    override suspend fun create(path: FolderPath, state: FolderState): Result<Folder> = runCatching {
        if (path in this) {
            throw FolderAlreadyExistsException(path)
        }
        if (path.parent !in this) {
            throw NodeParentNotFoundException(path.parent)
        }

        // Create the folder in the mounted repository.
        // The compositor state is then updated by the event handler for that repository.
        val (mount, repositoryPath) = toRepositoryLocation(path)
        when (repositoryPath) {
            is FolderPath -> mount.repository.create(repositoryPath, state).getOrThrow()
            RootPath -> {
                // This means that this path is mapped to a root path of a mounted repository, which always exist.
                throw NodeParentAlreadyExistsException(repositoryPath)
            }
        }

        return@runCatching Folder(this, path)
    }

    override suspend fun create(path: FolderPath, state: TreeState): Result<Folder> = runCatching {
        if (path in this) {
            throw FolderAlreadyExistsException(path)
        }
        if (path.parent !in this) {
            throw NodeParentNotFoundException(path.parent)
        }

        // Create the folder and its content in the mounted repository.
        // The compositor state is then updated by the event handler for that repository.
        val (mount, repositoryPath) = toRepositoryLocation(path)
        when (repositoryPath) {
            is FolderPath -> mount.repository.create(repositoryPath, state).getOrThrow()
            RootPath -> mount.repository.update(RootPath, state).getOrThrow()
        }

        return@runCatching Folder(this, path)
    }

    override suspend fun update(path: WarpPath, newState: WarpState): Result<Unit> = runCatching {
        if (path !in this) {
            throw WarpNotFoundException(path)
        }

        // Update the warp in the mounted repository.
        // The compositor state is then updated by the event handler for that repository.
        val (mount, repositoryPath) = toRepositoryLocation(path)
        mount.repository.update(repositoryPath, newState).getOrThrow()
    }

    override suspend fun update(path: FolderPath, newState: FolderState): Result<Unit> = runCatching {
        if (path !in this) {
            throw FolderNotFoundException(path)
        }

        val (mount, repositoryPath) = toRepositoryLocation(path)
        if (path == mount.targetPath) {
            // Update the mount state instead of the underlying repository state.
            mounts.remove(mount.targetPath)
            mounts[mount.targetPath] = mount.copy(state = newState)
            eventScope.post(FolderStateChangeEvent(this[path]!!, newState = newState, oldState = mount.state))
            return@runCatching
        }

        // Update the container in the mounted repository.
        // The compositor state is then updated by the event handler for that repository.
        mount.repository.update(repositoryPath, newState).getOrThrow()
    }

    override suspend fun update(path: RootPath, newState: FolderState): Result<Unit> = runCatching {
        val mount = mountFor(RootPath)
        val repositoryPath = mount.sourcePath
        if (path == mount.targetPath) {
            // Update the overlay instead of the underlying repository state.
            mounts.remove(mount.targetPath)
            mounts[mount.targetPath] = mount.copy(state = newState)
            eventScope.post(RootStateChangeEvent(root, newState = newState, oldState = mount.state))
            return@runCatching
        }

        // Update the container in the mounted repository.
        // The compositor state is then updated by the event handler for that repository.
        mount.repository.update(repositoryPath, newState).getOrThrow()
    }

    override suspend fun delete(path: WarpPath): Result<WarpState> = runCatching {
        // Delete the warp in the mounted repository.
        // The compositor state is then updated by the event handler for that repository.
        val (mount, repositoryPath) = toRepositoryLocation(path)
        mount.repository.delete(repositoryPath).getOrElse { return Result.failure(it) }
    }

    override suspend fun delete(path: FolderPath): Result<FolderState> = runCatching {
        val previousState = state[path] ?: throw FolderNotFoundException(path)
        val (mount, repositoryPath) = toRepositoryLocation(path)

        if (path == mount.targetPath) {
            // Deletion is handled by removing the repository mount in the next step.
            // This means the repository content is never actually modified, therefore we need to manually call the delete event.
            eventScope.post(FolderDeleteEvent(Folder(this, path)))
        }

        // Unmount all mounts whose target path is a subpath of path.
        val removedMounts = mounts.values
            .filter { it.targetPath.isSubpathOf(path) }
            .sortedByDescending { it.targetPath.value.length }
        for (mount in removedMounts) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount))
            mounts.remove(mount.targetPath)
        }

        if (path == mount.targetPath) {
            // Deletion was handled by removing the repository mount in the previous step.
            // This means the repository content was never actually modified, therefore we need to manually call the post delete event.
            eventScope.post(FolderPostDeleteEvent(path, previousState))
            return@runCatching previousState
        } else {
            // Delete the folder in the repository.
            // The compositor state is then updated by the event handler for that repository.
            // Because the path is not the mounts target path, repository path can at this point never be a root path.
            return@runCatching mount.repository.delete(repositoryPath as FolderPath).getOrThrow()
        }
    }

    override suspend fun move(
        src: WarpPath,
        dst: WarpPath,
    ): Result<Unit> = runCatching {
        // Early exit if move operation is a no-op.
        if (src == dst) {
            return@runCatching
        }

        if (src !in this) {
            throw WarpNotFoundException(src)
        }
        if (dst in this) {
            throw WarpAlreadyExistsException(src)
        }

        val (srcMount, srcRepositoryPath) = toRepositoryLocation(src)
        val (dstMount, dstRepositoryPath) = toRepositoryLocation(dst)

        if (srcMount.targetPath == dstMount.targetPath) {
            // The warp doesn't change its mount during the move operation, the move is therefore handled by the repository.
            val mount = srcMount // = dstMount
            mount.repository.move(srcRepositoryPath, dstRepositoryPath).getOrThrow()
        } else {
            // Move the warp, by deleting it from one repository and creating it in the other.
            // We mark the movement, so that the creation / deletion events of the repositories are transformed into a single move event.
            val movement = Movement(from = src, to = dst)
            crossRepositoryWarpMoves += movement
            try {
                val state = srcMount.repository.delete(srcRepositoryPath).getOrThrow()
                dstMount.repository.create(dstRepositoryPath, state).onFailure { exception ->
                    // Try to restore the warp at the previous location, then rethrow the exception.
                    srcMount.repository.create(dstRepositoryPath, state).onFailure {
                        logger.error("Failed to restore warp $src after failed move to $dst", it)
                    }
                    throw exception
                }
            } finally {
                crossRepositoryWarpMoves -= movement
            }
        }
    }

    override suspend fun move(
        src: FolderPath,
        dst: FolderPath,
    ): Result<Unit> = move(src, dst, moveMounts = true)

    public suspend fun move(
        src: FolderPath,
        dst: FolderPath,
        moveMounts: Boolean = true,
    ): Result<Unit> = runCatching {
        // Early exit if move operation is a no-op.
        if (src == dst) {
            return@runCatching
        }

        if (src !in this) {
            throw FolderNotFoundException(src)
        }
        if (dst in this) {
            throw FolderAlreadyExistsException(src)
        }

        // Check that the destination is not a subpath of the source.
        if (dst.isProperSubpathOf(src)) {
            throw FolderMoveIntoChildException(src, dst)
        }

        // Create a backup of the current state in case of a rollback.
        val rollbackState = state.copy()

        val (srcMount, srcRepositoryPath) = toRepositoryLocation(src)
        val (dstMount, dstRepositoryPath) = toRepositoryLocation(dst)
        check(dstRepositoryPath is FolderPath) // Always the case, as the destination does not exist at this point.
        if (srcMount.repository.id == dstMount.repository.id && dstRepositoryPath.isProperSubpathOf(srcRepositoryPath)) {
            // The move results in a repository destination path that is a subpath of the source in the repository.
            throw FolderMoveIntoChildException(src, dst)
        }

        // Collect all nodes that are being moved.
        val movedTree = subtree(src, includeNestedMounts = !moveMounts)!!

        // Collect all mounts that are affected by this move operation.
        val movedMounts = mounts()
            .filter { it.targetPath.isSubpathOf(src) }
            .sortedBy { it.targetPath.value.length }
        val rootIsMount = src in mounts.keys

        // Unmount all contained mounts.
        for (mount in movedMounts.reversed()) {
            eventScope.post(CompositorRepositoryUnmountEvent(this, mount))
            mounts.keys -= mount.targetPath

            // Remove mount content from the state.
            state.delete(mount.targetPath as FolderPath) // targetPath is a subpath of src which is a folder path.
        }

        // Move the folder.
        try {
            if (srcMount.repository.id == dstMount.repository.id) {
                // The source and destination repository are the same, the move can therefore be handled by the shared repository.
                check(srcRepositoryPath is FolderPath) // The source can't be a parent of the destination, and is therefore always a FolderPath.
                val mount = srcMount // = dstMount
                mount.repository.move(srcRepositoryPath, dstRepositoryPath).getOrThrow()
            } else {
                // The folder changes repository during the move, we therefore need to delete it in the old repository and create it in the new repository.
                if (!rootIsMount || !moveMounts) {
                    dstMount.repository.create(dstRepositoryPath, movedTree).getOrThrow()
                    if (!rootIsMount) {
                        check(srcRepositoryPath is FolderPath)
                        srcMount.repository.delete(srcRepositoryPath).getOrThrow()
                    }
                }
            }
        } catch (exception: Exception) {
            // Rollback to the previous state.
            this.state.update(rollbackState)
            for (mount in movedMounts) {
                mounts[mount.targetPath] = mount
                eventScope.post(CompositorRepositoryMountEvent(this, mount))
            }

            // Rethrow the exception.
            throw exception
        }

        if (moveMounts) {
            // Mount all mounts at their new locations.
            for (oldMount in movedMounts) {
                val newMount = oldMount.copy(
                    targetPath = dst / (oldMount.sourcePath.relativeTo(src)!!),
                )
                state[newMount.targetPath] = newMount.repository.state.subtree(newMount.sourcePath) ?: run {
                    // The source did already exist before and wasn't modified during the move.
                    logger.warn("Mount ${newMount.targetPath} is missing source ${newMount.repository.id}:${newMount.sourcePath}")
                    TreeState.empty()
                }
                mounts[newMount.targetPath] = newMount
                eventScope.post(CompositorRepositoryMountEvent(this, newMount))
            }

            // If the move root was a mount point, then no repository was ever modified.
            // We therefore need to manually call the move event.
            if (rootIsMount) {
                eventScope.post(FolderPathChangeEvent(Folder(this, dst), newPath = dst, oldPath = src))
            }
        }
    }

    /**
     * Returns the state of a subtree rooted at [root].
     *
     * @param includeNestedMounts Whether content from mounts nested below [root]
     * is included in the returned tree. If `false`, the subtree is limited to
     * nodes originating from the same mount as [root].
     */
    private fun subtree(
        root: NodeParentPath,
        includeNestedMounts: Boolean = true,
    ): TreeState? {
        // Collect all folders whose content should be ignored for the selection.
        val excludedPaths = if (includeNestedMounts) {
            emptySet()
        } else {
            mounts.keys.filter { it.isProperSubpathOf(root) }.toSet()
        }

        return state.subtree(root, excludedPaths)
    }

    // endregion

    /**
     * Context used when modifying the mount list.
     */
    public class MountModificationContext(mounts: Collection<CompositorMount>) {
        private val mounts = mounts.associateBy { it.targetPath }.toMutableMap()

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
        public fun register(
            path: NodeParentPath,
            repository: Repository<*, *>,
            repositoryPath: NodeParentPath,
            state: FolderState = FolderState.defaultMountState(),
        ): CompositorMount? {
            return mounts.put(path, CompositorMount(path, repository, repositoryPath, state))
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
        private val logger: Logger = LoggerFactory.getLogger(Compositor::class.java)
    }
}
