package net.voxelpi.varp.cli.util

import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.MutableCommandBuilder

/**
 * Add a new flag component to this command
 *
 * @param name name of the flag
 * @param aliases flag aliases
 * @param description description of the flag
 * @param mutator of the component supplier for the flag
 * @param <T> the component value type
 * @return this mutable builder
 */
fun <C : Any, T> MutableCommandBuilder<C>.valueFlag(
    name: String,
    aliases: Array<String> = emptyArray(),
    description: Description = Description.empty(),
    mutator: CommandComponent.Builder<C, T>.() -> Unit = {},
): MutableCommandBuilder<C> {
    val component = CommandComponent.builder<C, T>()
        .name(name)
        .also(mutator)
        .build()

    return flag(name, aliases, description, component)
}
