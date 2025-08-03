package net.voxelpi.varp.repository.sql.table

import org.jetbrains.exposed.v1.core.Table

object FolderTable : Table("varp.folder") {
    val path = text("path")
    val name = text("name")
    val description = text("description")
    val tags = text("tags")
    val properties = text("properties")

    override val primaryKey: PrimaryKey = PrimaryKey(path)
}
