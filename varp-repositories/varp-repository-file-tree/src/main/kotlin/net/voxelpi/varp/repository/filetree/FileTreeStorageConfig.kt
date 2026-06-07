package net.voxelpi.varp.repository.filetree

import java.nio.file.Path
import kotlin.io.path.div

@JvmRecord
data class FileTreeStorageConfig(
    val path: Path,
    val format: FileTreeStorageFormat,
) {

    fun dataDirectory(): Path {
        return path / "data"
    }

    fun tempDirectory(): Path {
        return path / "temp"
    }
}
