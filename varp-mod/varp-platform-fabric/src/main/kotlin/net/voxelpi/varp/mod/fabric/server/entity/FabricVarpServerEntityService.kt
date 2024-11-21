package net.voxelpi.varp.mod.fabric.server.entity

import net.minecraft.entity.Entity
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.server.entity.VarpServerEntityServiceImpl
import java.util.UUID

class FabricVarpServerEntityService(
    val server: FabricVarpServer,
) : VarpServerEntityServiceImpl() {

    fun entity(entity: Entity): FabricVarpServerEntity {
        return FabricVarpServerEntity(server, entity)
    }

    override fun entity(uniqueId: UUID): FabricVarpServerEntity? {
        for (world in server.server.worlds) {
            val entity = world.getEntity(uniqueId)
            if (entity != null) {
                return entity(entity)
            }
        }
        return null
    }
}
