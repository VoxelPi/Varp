package net.voxelpi.varp.mod.server.api.entity

import java.util.UUID

public interface VarpServerEntityService {

    public fun entity(uniqueId: UUID): VarpServerEntity?
}
