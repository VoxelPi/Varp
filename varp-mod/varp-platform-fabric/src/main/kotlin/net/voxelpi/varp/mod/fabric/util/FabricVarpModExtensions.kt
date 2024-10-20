package net.voxelpi.varp.mod.fabric.util

import net.kyori.adventure.key.Key
import net.minecraft.util.Identifier

fun Key.toIdentifier(): Identifier {
    return Identifier.of(namespace(), value())
}

fun Identifier.toKey(): Key {
    return Key.key(namespace, path)
}
