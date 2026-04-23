package net.voxelpi.varp.mod.fabric.util

import net.kyori.adventure.key.Key
import net.minecraft.resources.Identifier

fun Key.toIdentifier(): Identifier {
    return Identifier.fromNamespaceAndPath(namespace(), value())
}

fun Identifier.toKey(): Key {
    return Key.key(namespace, path)
}
