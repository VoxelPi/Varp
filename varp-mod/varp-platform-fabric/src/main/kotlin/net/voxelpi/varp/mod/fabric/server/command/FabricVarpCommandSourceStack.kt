package net.voxelpi.varp.mod.fabric.server.command

import net.minecraft.server.command.ServerCommandSource
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack

class FabricVarpCommandSourceStack(
    val sourceStack: ServerCommandSource,
) : VarpCommandSourceStack
