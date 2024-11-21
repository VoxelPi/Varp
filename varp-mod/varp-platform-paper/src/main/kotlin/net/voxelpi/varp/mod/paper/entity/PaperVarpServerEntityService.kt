package net.voxelpi.varp.mod.paper.entity

import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.server.entity.VarpServerEntityImpl
import net.voxelpi.varp.mod.server.entity.VarpServerEntityServiceImpl
import org.bukkit.entity.Entity
import java.util.UUID

class PaperVarpServerEntityService(
    val server: PaperVarpServer,
) : VarpServerEntityServiceImpl() {

    fun entity(entity: Entity): PaperVarpServerEntity {
        return PaperVarpServerEntity(server, entity)
    }

    override fun entity(uniqueId: UUID): VarpServerEntityImpl? {
        return server.server.getEntity(uniqueId)?.let(::entity)
    }
}
