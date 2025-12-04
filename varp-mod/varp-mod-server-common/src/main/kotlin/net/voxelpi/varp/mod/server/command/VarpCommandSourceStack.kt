package net.voxelpi.varp.mod.server.command

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.server.actor.VarpActor
import net.voxelpi.varp.mod.server.api.entity.VarpServerEntity
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import net.voxelpi.varp.mod.server.command.exception.EntityExecutorRequiredException
import net.voxelpi.varp.mod.server.command.exception.PlayerExecutorRequiredException

interface VarpCommandSourceStack : VarpActor, ForwardingAudience.Single {

    val location: MinecraftLocation

    val sender: Audience

    fun playerOrNull(): VarpServerPlayer?

    fun playerOrThrow(): VarpServerPlayer {
        return playerOrNull() ?: throw PlayerExecutorRequiredException()
    }

    fun entityOrNull(): VarpServerEntity?

    fun entityOrThrow(): VarpServerEntity {
        return entityOrNull() ?: throw EntityExecutorRequiredException()
    }

    override fun audience(): Audience {
        return sender
    }
}
