package net.voxelpi.varp.repository.filetree

import net.voxelpi.varp.warp.repository.TreeRepositoryType

object FileTreeTreeRepositoryType : TreeRepositoryType<FileTreeTreeRepository, FileTreeTreeRepositoryConfig> {

    override val id: String = "file-tree"

    override fun createRepository(id: String, config: FileTreeTreeRepositoryConfig): FileTreeTreeRepository {
        return FileTreeTreeRepository(id, TODO(), config.format)
    }
}
