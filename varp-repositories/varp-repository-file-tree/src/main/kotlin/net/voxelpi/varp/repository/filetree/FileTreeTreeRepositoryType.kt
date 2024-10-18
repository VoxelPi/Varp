package net.voxelpi.varp.repository.filetree

import net.voxelpi.varp.warp.repository.TreeRepositoryType

object FileTreeTreeRepositoryType : TreeRepositoryType<FileTreeTreeRepository, FileTreeTreeRepositoryConfig>("file-tree", FileTreeTreeRepositoryConfig::class.java) {

    override fun createRepository(id: String, config: FileTreeTreeRepositoryConfig): FileTreeTreeRepository {
        return FileTreeTreeRepository(id, TODO(), config.format)
    }
}
