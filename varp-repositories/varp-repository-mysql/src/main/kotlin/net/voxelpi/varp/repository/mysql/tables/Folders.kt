package net.voxelpi.varp.repository.mysql.tables

import org.jetbrains.exposed.sql.Table

object Folders : Table("folders") {
    val path = varchar("path", 512)
    val name = text("name")
    val description = text("description")
    val tags = text("tags")
    val properties = text("properties")

    override val primaryKey: PrimaryKey = PrimaryKey(path)
}
