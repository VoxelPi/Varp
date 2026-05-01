package net.voxelpi.varp.mod.paper.command

import org.incendo.cloud.paper.PaperCommandManager

interface PaperVarpCommand {

    fun register(manager: PaperCommandManager<PaperVarpCommandSourceStack>)
}
