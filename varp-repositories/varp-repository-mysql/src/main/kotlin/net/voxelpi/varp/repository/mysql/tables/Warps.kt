package net.voxelpi.varp.repository.mysql.tables

import org.jetbrains.exposed.sql.Table

object Warps : Table("warps") {
    val path = varchar("path", 512)
    val name = text("name")
    val description = text("description")
    val tags = text("tags")
    val properties = text("properties")
    val world = varchar("world", 256)
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")

    override val primaryKey: PrimaryKey = PrimaryKey(path)
}
