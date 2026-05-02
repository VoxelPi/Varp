package net.voxelpi.varp.tree.state

import net.voxelpi.varp.ComponentTemplate

/**
 * The state of a folder.
 * @property name the name of the folder.
 * @property description the description of the folder.
 * @property tags a set of all tags of the folder.
 * @property properties a map of all properties of the folder.
 */
@JvmRecord
public data class FolderState(
    override val name: ComponentTemplate,
    override val description: List<ComponentTemplate> = emptyList(),
    override val tags: Set<String> = emptySet(),
    override val properties: Map<String, String> = emptyMap(),
) : NodeState {

    public fun modifiedCopy(action: Builder.() -> Unit): FolderState {
        return Builder(this).apply(action).build()
    }

    public data class Builder(
        override var name: ComponentTemplate,
        override var description: MutableList<ComponentTemplate>,
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

    public companion object {

        public fun defaultRootState(): FolderState {
            return FolderState(ComponentTemplate("root"), emptyList(), setOf("varp:root"), emptyMap())
        }
    }
}
