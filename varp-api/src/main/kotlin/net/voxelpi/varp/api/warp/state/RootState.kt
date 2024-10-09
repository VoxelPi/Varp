package net.voxelpi.varp.api.warp.state

import net.kyori.adventure.text.Component

/**
 * The state of the root.
 * @property name the name of the root.
 * @property description the description of the root.
 * @property tags a set of all tags of the root.
 * @property properties a map of all properties of the root.
 */
@JvmRecord
data class RootState(
    override val name: Component,
    override val description: List<Component> = emptyList(),
    override val tags: Set<String> = emptySet(),
    override val properties: Map<String, String> = emptyMap(),
) : NodeState {

    data class Builder(
        override var name: Component,
        override var description: MutableList<Component>,
        override var tags: MutableSet<String>,
        override var properties: MutableMap<String, String>,
    ) : NodeState.Builder {
        constructor(state: RootState) : this(
            state.name,
            state.description.toMutableList(),
            state.tags.toMutableSet(),
            state.properties.toMutableMap(),
        )

        override fun build(): RootState {
            return RootState(name, description, tags, properties)
        }
    }
}
