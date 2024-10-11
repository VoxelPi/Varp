package net.voxelpi.varp.warp.state

import net.kyori.adventure.text.Component

/**
 * The state of a folder.
 * @property name the name of the folder.
 * @property description the description of the folder.
 * @property tags a set of all tags of the folder.
 * @property properties a map of all properties of the folder.
 */
@JvmRecord
public data class FolderState(
    override val name: Component,
    override val description: List<Component> = emptyList(),
    override val tags: Set<String> = emptySet(),
    override val properties: Map<String, String> = emptyMap(),
) : NodeState {

    public data class Builder(
        override var name: Component,
        override var description: MutableList<Component>,
        override var tags: MutableSet<String>,
        override var properties: MutableMap<String, String>,
    ) : NodeState.Builder {
        public constructor(state: FolderState) : this(
            state.name,
            state.description.toMutableList(),
            state.tags.toMutableSet(),
            state.properties.toMutableMap(),
        )

        override fun build(): FolderState {
            return FolderState(name, description, tags, properties)
        }
    }
}
