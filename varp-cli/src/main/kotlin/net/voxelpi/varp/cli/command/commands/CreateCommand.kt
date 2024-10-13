package net.voxelpi.varp.cli.command.commands

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.cli.command.VarpCLICommandSender
import net.voxelpi.varp.cli.command.parser.keyParser
import net.voxelpi.varp.cli.command.parser.tree.nodeParentParser
import net.voxelpi.varp.cli.util.valueFlag
import net.voxelpi.varp.warp.NodeParent
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.parser
import org.incendo.cloud.parser.standard.DoubleParser.doubleParser
import org.incendo.cloud.parser.standard.FloatParser.floatParser
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser

object CreateCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        event.commandManager.buildAndRegister("create") {
            literal("warp")
            required("parent", nodeParentParser { event.cli.tree })
            required("id", stringParser())

            required("world", keyParser())
            required("x", doubleParser())
            required("y", doubleParser())
            required("z", doubleParser())
            required("yaw", floatParser())
            required("pitch", floatParser())

            valueFlag<VarpCLICommandSender, String>("name", aliases = arrayOf("n")) {
                parser = quotedStringParser()
            }

            handler { context ->
                val parent: NodeParent = context["parent"]
                val id: String = context["id"]

                val name = miniMessage().deserialize(context.flags().getValue<String>("name").orElse(id))

                val state = WarpState(
                    MinecraftLocation(
                        Key.key("varp:test"),
                        2.0,
                        3.0,
                        3.0,
                        2f,
                        3f
                    ),
                    name,
                )
                parent.createWarp(id, state)
            }
        }

        event.commandManager.buildAndRegister("create") {
            literal("folder")
            required("parent", nodeParentParser { event.cli.tree })
            required("id", stringParser())

            valueFlag<VarpCLICommandSender, String>("name", aliases = arrayOf("n")) {
                parser = quotedStringParser()
            }

            handler { context ->
                val parent: NodeParent = context["parent"]
                val id: String = context["id"]

                val name = miniMessage().deserialize(context.flags().getValue<String>("name").orElse(id))

                val state = FolderState(
                    name,
                )
                parent.createFolder(id, state)
            }
        }
    }
}
