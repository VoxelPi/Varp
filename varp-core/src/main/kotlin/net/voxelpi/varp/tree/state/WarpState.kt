package net.voxelpi.varp.tree.state

import net.kyori.adventure.text.Component
import net.voxelpi.varp.MinecraftLocation

/**
 * The state of a warp.
 * @property location the location of the warp.
 * @property name the name of the warp.
 * @property description the description of the warp.
 * @property tags a set of all tags of the warp.
 * @property properties a map of all properties of the warp.
 */
@JvmRecord
public data class WarpState(
    val location: MinecraftLocation,
    override val name: Component,
    override val description: List<Component> = emptyList(),
    override val tags: Set<String> = emptySet(),
    override val properties: Map<String, String> = emptyMap(),
) : NodeState {

    public data class Builder(
        var location: MinecraftLocation,
        override var name: Component,
        override var description: MutableList<Component>,
        override var tags: MutableSet<String>,
        override var properties: MutableMap<String, String>,
    ) : NodeState.Builder {

        public constructor(state: WarpState) : this(
            state.location,
            state.name,
            state.description.toMutableList(),
            state.tags.toMutableSet(),
            state.properties.toMutableMap(),
        )

        override fun build(): WarpState {
            return WarpState(location, name, description, tags, properties)
        }
    }
}
