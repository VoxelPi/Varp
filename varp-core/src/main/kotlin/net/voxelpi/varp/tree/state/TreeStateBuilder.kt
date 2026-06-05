package net.voxelpi.varp.tree.state

import net.kyori.adventure.key.Key
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath

public class TreeBuilder {

    internal val warps: MutableMap<String, WarpState> = mutableMapOf()
    internal val folders: MutableMap<String, Pair<FolderState, TreeBuilder>> = mutableMapOf()

    /**
     * Adds a warp with the given [id] and [state] to the current parent.
     */
    public fun warp(
        id: String,
        state: WarpState,
    ): WarpState {
        warps[id] = state
        return state
    }

    /**
     * Adds a warp with the given [id] and state to the current parent.
     */
    public fun warp(
        id: String,
        location: MinecraftLocation,
        name: ComponentTemplate,
        description: List<ComponentTemplate> = emptyList(),
        tags: Set<String> = emptySet(),
        properties: Map<String, String> = emptyMap(),
    ): WarpState = warp(id, WarpState(location, name, description, tags, properties))

    /**
     * Adds a warp with the given [id] and state to the current parent.
     */
    public fun warp(
        id: String,
        location: MinecraftLocation,
        name: String = id,
        description: List<String> = emptyList(),
        tags: Set<String> = emptySet(),
        properties: Map<String, String> = emptyMap(),
    ): WarpState = warp(id, WarpState(location, name, description, tags, properties))

    /**
     * Adds a warp with the given [id] and state to the current parent.
     */
    public fun warp(
        id: String,
        world: Key,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float = 0f,
        pitch: Float = 0f,
        name: String = id,
        description: List<String> = emptyList(),
        tags: Set<String> = emptySet(),
        properties: Map<String, String> = emptyMap(),
    ): WarpState = warp(id, WarpState(world, x, y, z, yaw, pitch, name, description, tags, properties))

    /**
     * Adds a folder with the given [id] and [state] to the current parent.
     */
    public fun folder(
        id: String,
        state: FolderState,
        content: TreeBuilder.() -> Unit,
    ): FolderState {
        val contentBuilder = TreeBuilder()
        contentBuilder.content()
        folders[id] = Pair(state, contentBuilder)
        return state
    }

    /**
     * Adds a folder with the given [id] and state to the current parent.
     */
    public fun folder(
        id: String,
        name: ComponentTemplate,
        description: List<ComponentTemplate> = emptyList(),
        tags: Set<String> = emptySet(),
        properties: Map<String, String> = emptyMap(),
        content: TreeBuilder.() -> Unit,
    ): FolderState = folder(id, FolderState(name, description, tags, properties), content)

    /**
     * Adds a folder with the given [id] and state to the current parent.
     */
    public fun folder(
        id: String,
        name: String = id,
        description: List<String> = emptyList(),
        tags: Set<String> = emptySet(),
        properties: Map<String, String> = emptyMap(),
        content: TreeBuilder.() -> Unit,
    ): FolderState = folder(id, FolderState(name, description, tags, properties), content)
}

/**
 * Creates a new tree state.
 */
public fun treeState(
    root: FolderState,
    content: TreeBuilder.() -> Unit,
): TreeState {
    val rootBuilder = TreeBuilder()
    rootBuilder.content()

    val warps: MutableMap<WarpPath, WarpState> = mutableMapOf()
    val folders: MutableMap<FolderPath, FolderState> = mutableMapOf()

    val queue = ArrayDeque<Pair<NodeParentPath, TreeBuilder>>(listOf(Pair(RootPath, rootBuilder)))
    while (queue.isNotEmpty()) {
        val (parent, builder) = queue.removeFirst()
        for ((id, state) in builder.warps) {
            val path = parent.warp(id)
            if (path in warps) {
                throw WarpAlreadyExistsException(path)
            }

            warps[path] = state
        }
        for ((id, data) in builder.folders) {
            val (state, contentBuilder) = data
            val path = parent.folder(id)
            if (path in folders) {
                throw FolderAlreadyExistsException(path)
            }

            folders[path] = state
            queue.addLast(path to contentBuilder)
        }
    }

    return MutableTreeState(
        warps,
        folders,
        root,
    )
}

/**
 * Creates a new tree state.
 */
public fun treeState(
    name: ComponentTemplate,
    description: List<ComponentTemplate> = emptyList(),
    tags: Set<String> = emptySet(),
    properties: Map<String, String> = emptyMap(),
    content: TreeBuilder.() -> Unit,
): TreeState = treeState(FolderState(name, description, tags, properties), content)

/**
 * Creates a new tree state.
 */
public fun treeState(
    name: String = "root",
    description: List<String> = emptyList(),
    tags: Set<String> = emptySet(),
    properties: Map<String, String> = emptyMap(),
    content: TreeBuilder.() -> Unit,
): TreeState = treeState(FolderState(ComponentTemplate(name), description.map { ComponentTemplate((it)) }, tags, properties), content)
