package net.voxelpi.varp.mod.fabric.server.command

import net.minecraft.server.command.ServerCommandSource
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack

class FabricVarpCommandSourceStack(
    val sourceStack: ServerCommandSource,
) : VarpCommandSourceStack {

    override val location: MinecraftLocation
        get() = MinecraftLocation(
            sourceStack.world.registryKey.key(),
            sourceStack.position.x,
            sourceStack.position.y,
            sourceStack.position.z,
            sourceStack.rotation.y,
            sourceStack.rotation.x,
        )
}
