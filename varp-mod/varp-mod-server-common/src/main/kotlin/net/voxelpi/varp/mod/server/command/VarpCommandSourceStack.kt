package net.voxelpi.varp.mod.server.command

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.server.actor.VarpServerActor
import net.voxelpi.varp.mod.server.command.exception.EntityExecutorRequiredException
import net.voxelpi.varp.mod.server.command.exception.PlayerExecutorRequiredException
import net.voxelpi.varp.mod.server.entity.VarpServerEntityImpl
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl

interface VarpCommandSourceStack : VarpServerActor, ForwardingAudience.Single {

    val location: MinecraftLocation

    val sender: Audience

    fun playerOrNull(): VarpServerPlayerImpl?

    fun playerOrThrow(): VarpServerPlayerImpl {
        return playerOrNull() ?: throw PlayerExecutorRequiredException()
    }

    fun entityOrNull(): VarpServerEntityImpl?

    fun entityOrThrow(): VarpServerEntityImpl {
        return entityOrNull() ?: throw EntityExecutorRequiredException()
    }

    override fun audience(): Audience {
        return sender
    }
}
