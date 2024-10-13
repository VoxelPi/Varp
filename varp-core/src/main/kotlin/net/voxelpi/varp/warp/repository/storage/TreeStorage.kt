package net.voxelpi.varp.warp.repository.storage

import net.voxelpi.varp.warp.repository.TreeRepository
import java.nio.file.Path

public interface TreeStorage : TreeRepository {

    public val directory: Path
}
