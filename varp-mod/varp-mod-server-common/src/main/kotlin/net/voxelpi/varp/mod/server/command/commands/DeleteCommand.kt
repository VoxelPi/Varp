package net.voxelpi.varp.mod.server.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.voxelpi.varp.extras.cloud.parser.tree.nodeChildParser
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.NodeChild
import net.voxelpi.varp.warp.Warp
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object DeleteCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.create.folder")

            literal("delete")

            required("node", nodeChildParser { serverProvider().tree })

            handler { context ->
                val server = serverProvider()
                val node: NodeChild = context["node"]

                // Delete the node.
                server.coroutineScope.launch(Dispatchers.IO) {
                    when (node) {
                        is Folder -> context.sender().deleteFolder(node)
                        is Warp -> context.sender().deleteWarp(node)
                    }
                }
            }
        }
    }
}
