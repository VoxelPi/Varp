package net.voxelpi.varp.repository.filetree

import net.voxelpi.varp.warp.repository.RepositoryConfig
import java.nio.file.Path

data class FileTreeRepositoryConfig(
    val path: Path,
    val format: String,
    val componentsAsObjects: Boolean,
) : RepositoryConfig
