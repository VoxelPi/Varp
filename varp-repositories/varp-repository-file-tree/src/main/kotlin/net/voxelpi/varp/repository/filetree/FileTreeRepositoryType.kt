package net.voxelpi.varp.repository.filetree

import net.voxelpi.varp.repository.RepositoryType

object FileTreeRepositoryType : RepositoryType<FileTreeRepository, FileTreeRepositoryConfig>("file-tree", FileTreeRepository::class, FileTreeRepositoryConfig::class) {

    override fun create(id: String, config: FileTreeRepositoryConfig): Result<FileTreeRepository> {
        return Result.success(FileTreeRepository(id, config))
    }
}
