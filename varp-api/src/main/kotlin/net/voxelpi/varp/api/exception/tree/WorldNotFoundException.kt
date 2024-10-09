package net.voxelpi.varp.api.exception.tree

import net.kyori.adventure.key.Key

class WorldNotFoundException(val world: Key) : Exception("world $world doesn't exist")
