package net.voxelpi.varp.api.warp.state

import net.kyori.adventure.text.Component

/**
 * The state of a node.
 */
sealed interface NodeState {

    /**
     * The name of the node.
     */
    val name: Component

    /**
     * The description of the node.
     */
    val description: List<Component>

    /**
     * A set of all tags of the node.
     */
    val tags: Set<String>

    /**
     * A map of all properties of the node.
     */
    val properties: Map<String, String>

    /**
     * Checks if the node has the given [tag].
     * If [tag] is `null`, `true` is returned.
     */
    fun hasTag(tag: String?): Boolean {
        return tag == null || tag in tags
    }

    /**
     * Checks if the node has a property with the given [key].
     */
    fun hasProperty(key: String): Boolean {
        return properties.contains(key)
    }

    sealed interface Builder {

        var name: Component

        var description: MutableList<Component>

        var tags: MutableSet<String>

        var properties: MutableMap<String, String>

        fun build(): NodeState

        /**
         * Checks if the node has the given [tag].
         * If [tag] is `null`, `true` is returned.
         */
        fun hasTag(tag: String?): Boolean {
            return tag == null || tag in tags
        }

        /**
         * Adds the given [tag] to the warp.
         * @return `true` if the tag has been added, `false` if the node already has the given [tag].
         */
        fun addTag(tag: String): Boolean {
            return tags.add(tag)
        }

        /**
         * Removes the given [tag] from the warp.
         * @return `true` if the tag has been removed, `false` if the node didn't have the given [tag].
         */
        fun removeTag(tag: String): Boolean {
            return tags.remove(tag)
        }

        /**
         * Checks if the node has a property with the given [key].
         */
        fun hasProperty(key: String): Boolean {
            return properties.contains(key)
        }

        /**
         * Gets the value of the node with the given [key].
         */
        fun getProperty(key: String): String? {
            return properties[key]
        }

        /**
         * Sets the property with the given [key] to the given [value].
         * @return the previous value associated with the property [key], or `null` if the node didn't have a property with that name.
         */
        fun setProperty(key: String, value: String): String? {
            return properties.put(key, value)
        }

        /**
         * Removes the property with the given [key] from the warp.
         * @return the previous value of the property, or null if the warp had no property with the that name.
         */
        fun removeProperty(key: String): String? {
            return properties.remove(key)
        }
    }
}
