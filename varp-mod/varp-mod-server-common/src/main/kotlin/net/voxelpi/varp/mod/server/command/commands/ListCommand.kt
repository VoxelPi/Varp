package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.tree.path.NodeParentPath
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.argumentDescription
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.IntegerParser.integerParser
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import kotlin.jvm.optionals.getOrNull

object ListCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.list")

            literal("list")

            flag("path", arrayOf("p"), argumentDescription("The container which content should be listed"), StringParser.stringParser())
            flag("depth", arrayOf("d"), argumentDescription("The maximal recursive depth"), integerParser(0))
            flag("tag", arrayOf("t"), argumentDescription("Tag predicate"), quotedStringParser())

            handler { context ->
                val server = serverProvider()
                val tree = server.tree

                val pathString: String = context.flags().getValue<String>("path").getOrNull() ?: "/"
                val path = NodeParentPath.parse(pathString).getOrThrow()

                val parent = tree.resolve(path) ?: return@handler
                for (warp in parent.childWarps()) {
                    context.sender().sender.sendMessage(warp.name)
                }
            }
        }

        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.tree")

            literal("tree")

            flag("path", arrayOf("p"), argumentDescription("The container which content should be listed"), StringParser.stringParser())
            flag("depth", arrayOf("d"), argumentDescription("The maximal recursive depth"), integerParser(0))
            flag("tag", arrayOf("t"), argumentDescription("Tag predicate"), quotedStringParser())

            handler {}
        }
    }
}
