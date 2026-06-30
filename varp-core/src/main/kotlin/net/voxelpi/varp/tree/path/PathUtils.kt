package net.voxelpi.varp.tree.path

import net.voxelpi.varp.util.Movement
import kotlin.collections.filter

/**
 * Constructs a [WarpPath] from the given node ids.
 */
public fun warpPath(node1: String, vararg nodes: String): WarpPath {
    return WarpPath("/$node1/${nodes.joinToString("/")}")
}

/**
 * Constructs a [FolderPath] from the given node ids.
 */
public fun folderPath(node1: String, vararg nodes: String): FolderPath {
    return FolderPath("/$node1/${nodes.joinToString("/")}/")
}

/**
 * Constructs a [NodeParentPath] from the given node ids.
 */
public fun nodeParentPath(vararg nodes: String): NodeParentPath {
    return FolderPath("/${nodes.joinToString("/")}/")
}

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

/**
 * Resolves a collection of local subtree movements into effective global movements.
 *
 * A local movement describes the direct movement of a folder without accounting for other
 * movements that may affect one of its ancestors. For example, if a folder is moved from `/a/` to
 * `/b/`, then another movement targeting `/a/c/` must be translated so that its final destination is
 * under `/b/` instead.
 *
 * This function applies those ancestor movements and returns the effective movement for each input
 * movement.
 *
 * The function assumes that the relative hierarchy of the moved representatives is preserved. That
 * means that if movement `A` is located inside the source subtree of movement `B`, then `A.to` is
 * expected to still point into `B.from` before this function is applied. This function then rewrites
 * `A.to` so that it points into `B.to`.
 */
internal fun resolveLocalMovements(relativeMovements: Collection<Movement<FolderPath?>>): List<Movement<FolderPath?>> {
    val absoluteMovements = mutableListOf<Movement<FolderPath?>>()

    // Assumption: Hierarchy "stays the same", meaning if the source of a movement is a subpath from the source of another then the destination also stays a subpath.
    for (move in relativeMovements.sortedBy { it.from?.level ?: -1 }) {
        if (move.to == null) {
            // The node gets deleted, so it doesn't have to be transformed.
            absoluteMovements += move
            continue
        }

        val parentMove = absoluteMovements.lastOrNull { it.from != null && move.to.isSubpathOf(it.from) }
        if (parentMove == null) {
            // The move destination is not a subpath of another movement source, it therefore doesn't get transformed.
            absoluteMovements += move
            continue
        }

        if (parentMove.to == null) {
            // If the parent gets delete, the source also need to be deleted.
            absoluteMovements += Movement(move.from, null)
            continue
        }

        val transformedDestination = parentMove.to / move.to.relativeTo(parentMove.from!!)!! // We know that parentMove.from is non-null because of the filter.
        absoluteMovements += Movement(move.from, transformedDestination)
    }

    return absoluteMovements
}
