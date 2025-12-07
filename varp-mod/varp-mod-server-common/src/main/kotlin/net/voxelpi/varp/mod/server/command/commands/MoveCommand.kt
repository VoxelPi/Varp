package net.voxelpi.varp.mod.server.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.voxelpi.varp.extras.cloud.parser.tree.nodeChildParser
import net.voxelpi.varp.extras.cloud.parser.tree.nodeParentParser
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.NodeChild
import net.voxelpi.varp.tree.NodeParent
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object MoveCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.create.folder")

            literal("move")

            required("node", nodeChildParser { serverProvider().tree })
            required("destination", nodeParentParser { serverProvider().tree })

            handler { context ->
                val server = serverProvider()
                val node: NodeChild = context["node"]
                val destinationParent: NodeParent = context["destination"]

                // Delete the node.
                server.coroutineScope.launch(Dispatchers.IO) {
                    when (node) {
                        is Folder -> context.sender().moveFolder(node, destinationParent.path.folder(node.id))
                        is Warp -> context.sender().moveWarp(node, destinationParent.path.warp(node.id))
                    }
                }
            }
        }
    }
}
