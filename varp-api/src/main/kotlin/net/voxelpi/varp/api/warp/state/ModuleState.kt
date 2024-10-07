package net.voxelpi.varp.api.warp.state

import net.kyori.adventure.text.Component

/**
 * The state of a module.
 * @property name the name of the module.
 * @property description the description of the module.
 * @property tags a set of all tags of the module.
 * @property properties a map of all properties of the module.
 */
@JvmRecord
data class ModuleState(
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
        constructor(state: ModuleState) : this(
            state.name,
            state.description.toMutableList(),
            state.tags.toMutableSet(),
            state.properties.toMutableMap(),
        )

        override fun build(): ModuleState {
            return ModuleState(name, description, tags, properties)
        }
    }
}
