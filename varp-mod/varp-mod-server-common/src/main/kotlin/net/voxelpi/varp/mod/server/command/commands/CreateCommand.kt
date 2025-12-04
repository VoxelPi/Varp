package net.voxelpi.varp.mod.server.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.voxelpi.varp.extras.cloud.parser.tree.nodeParentParser
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.warp.NodeParent
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.argumentDescription
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser

object CreateCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.create.folder")

            literal("create")
            literal("folder")

            required("id", stringParser())
            optional("parent", nodeParentParser { serverProvider().tree })

            flag("name", arrayOf("n"), argumentDescription("The name of the folder"), quotedStringParser())

            handler { context ->
                val server = serverProvider()

                // Construct the folder path.
                val parent: NodeParent = context.getOrDefault("parent", server.tree.root)
                val id: String = context["id"]
                val path = parent.path.folder(id)

                // Construct the folder state.
                val name = miniMessage().deserialize(context.flags().getValue<String>("name").orElse(id))
                val state = FolderState(
                    name,
                )

                // Create the folder.
                server.coroutineScope.launch(Dispatchers.IO) {
                    context.sender().createFolder(path, state)
                }
            }
        }

        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.create.warp")

            literal("create")
            literal("warp")

            required("id", stringParser())
            optional("parent", nodeParentParser { serverProvider().tree })

            flag("name", arrayOf("n"), argumentDescription("The name of the warp"), quotedStringParser())

            handler { context ->
                val server = serverProvider()

                // Construct the warp path.
                val parent: NodeParent = context.getOrDefault("parent", server.tree.root)
                val id: String = context["id"]
                val path = parent.path.warp(id)

                // Construct the warp state.
                val name = miniMessage().deserialize(context.flags().getValue<String>("name").orElse(id))
                val state = WarpState(
                    context.sender().location,
                    name,
                )

                // Create the warp.
                server.coroutineScope.launch(Dispatchers.IO) {
                    context.sender().createWarp(path, state)
                }
            }
        }
    }
}
