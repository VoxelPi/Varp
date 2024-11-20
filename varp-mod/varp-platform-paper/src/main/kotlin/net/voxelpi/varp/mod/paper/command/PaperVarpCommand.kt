package net.voxelpi.varp.mod.paper.command

import net.voxelpi.varp.mod.paper.PaperVarpServer
import org.incendo.cloud.paper.PaperCommandManager

interface PaperVarpCommand {

    fun register(manager: PaperCommandManager<PaperVarpCommandSourceStack>, serverProvider: () -> PaperVarpServer)
}
