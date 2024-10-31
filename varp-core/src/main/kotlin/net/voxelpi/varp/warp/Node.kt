package net.voxelpi.varp.warp

import net.kyori.adventure.text.Component
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.state.NodeState

public sealed interface Node {

    /**
     * The tree this node belongs to.
     */
    public val tree: Tree

    /**
     * The path to the node.
     */
    public val path: NodePath

    /**
     * The state of the node.
     */
    public val state: NodeState

    /**
     * The name of the node.
     */
    public val name: Component
        get() = state.name

    /**
     * The description of the node.
     */
    public val description: List<Component>
        get() = state.description

    /**
     * A set of all tags of the node.
     */
    public val tags: Set<String>
        get() = state.tags

    /**
     * A map of all properties of the node.
     */
    public val properties: Map<String, String>
        get() = state.properties
}
