package net.voxelpi.varp.repository.sql.table

import org.jetbrains.exposed.v1.core.Table

object WarpTable : Table("varp.warp") {
    val path = text("path")
    val name = text("name")
    val description = text("description")
    val tags = text("tags")
    val properties = text("properties")
    val world = text("world")
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val yaw = float("yaw")
    val pitch = float("pitch")

    override val primaryKey: PrimaryKey = PrimaryKey(path)
}
