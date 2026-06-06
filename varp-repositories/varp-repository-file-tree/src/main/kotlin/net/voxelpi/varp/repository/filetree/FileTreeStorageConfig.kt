package net.voxelpi.varp.repository.filetree

import java.nio.file.Path

@JvmRecord
data class FileTreeStorageConfig(
    val path: Path,
    val format: FileTreeStorageFormat,
)
