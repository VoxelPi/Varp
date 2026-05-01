package net.voxelpi.varp.mod.server.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.voxelpi.varp.extras.cloud.parser.tree.nodeChildParser
import net.voxelpi.varp.extras.cloud.parser.tree.nodeParentParser
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.NodeChild
import net.voxelpi.varp.tree.NodeParent
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.argumentDescription
import org.incendo.cloud.kotlin.extension.buildAndRegister

object CopyCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.copy")

            literal("copy")

            required("node", nodeChildParser())
            required("destination", nodeParentParser())

            flag("recursive", arrayOf("r"), argumentDescription("If child nodes should be copied"))

            handler { context ->
                val coroutineScope = context[VarpModCommandArguments.COROUTINE_SCOPE]
                val node: NodeChild = context["node"]
                val destinationParent: NodeParent = context["destination"]
                val recursive = context.flags().isPresent("recursive")

                // Delete the node.
                coroutineScope.launch(Dispatchers.IO) {
                    when (node) {
                        is Folder -> context.sender().copyFolder(node, destinationParent.path.folder(node.id), recursive)
                        is Warp -> context.sender().copyWarp(node, destinationParent.path.warp(node.id))
                    }
                }
            }
        }
    }
}
