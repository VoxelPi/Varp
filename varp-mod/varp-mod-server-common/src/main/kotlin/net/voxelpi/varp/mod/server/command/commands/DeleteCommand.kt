package net.voxelpi.varp.mod.server.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.voxelpi.varp.extras.cloud.parser.tree.nodeChildParser
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.NodeChild
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object DeleteCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.create.folder")

            literal("delete")

            required("node", nodeChildParser())

            handler { context ->
                val node: NodeChild = context["node"]
                val coroutineScope = context[VarpModCommandArguments.COROUTINE_SCOPE]

                // Delete the node.
                coroutineScope.launch(Dispatchers.IO) {
                    when (node) {
                        is Folder -> context.sender().deleteFolder(node)
                        is Warp -> context.sender().deleteWarp(node)
                    }
                }
            }
        }
    }
}
