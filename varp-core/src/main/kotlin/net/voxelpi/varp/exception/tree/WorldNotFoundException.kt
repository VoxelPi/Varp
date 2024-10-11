package net.voxelpi.varp.exception.tree

import net.kyori.adventure.key.Key

public class WorldNotFoundException(
    public val world: Key,
) : Exception("world $world doesn't exist")
