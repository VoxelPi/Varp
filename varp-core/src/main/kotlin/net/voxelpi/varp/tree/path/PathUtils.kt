package net.voxelpi.varp.tree.path

import kotlin.collections.filter

@Suppress("UNCHECKED_CAST")
public fun <T : NodeParentPath> topLevelPaths(paths: Collection<T>): Set<T> {
    val paths = paths.toSet() as Set<NodeParentPath>

    // If the root path is present in this
    if (RootPath in paths) {
        return setOf(RootPath) as Set<T>
    }

    // Because the root path is not present in this collection, we can cast everything to folder paths.
    paths as Set<FolderPath>

    return paths.filter { path ->
        var node = path
        while (node.parent is FolderPath) {
            val parentPath = node.parent as FolderPath
            if (parentPath in paths) {
                return@filter false
            }
            node = parentPath
        }

        // The root path can't be in a folder path set.
        true
    }.toSet() as Set<T>
}
