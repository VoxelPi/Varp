package net.voxelpi.varp.mod.server.entity

import net.voxelpi.varp.mod.server.api.entity.VarpServerEntityService
import java.util.UUID

abstract class VarpServerEntityServiceImpl : VarpServerEntityService {

    abstract override fun entity(uniqueId: UUID): VarpServerEntityImpl?
}
