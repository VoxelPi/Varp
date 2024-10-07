package net.voxelpi.varp.api.warp.path

import net.voxelpi.varp.api.warp.exception.path.InvalidNodeParentPathException
import java.util.regex.Pattern

/**
 * A path to a node in the warp node tree.
 */
sealed interface NodePath {

    /**
     * The path of the node as string.
     */
    val value: String

    /**
     * The id of the node.
     */
    val id: String

    /**
     * The module of the node.
     */
    val module: ModulePath
        get() = ModulePath.module(value.substring(0, value.indexOf(":")))

    /**
     * The level of the node.
     */
    val level: Int

    companion object {
        private val PATH_PATTERN = Pattern.compile("^([a-z0-9_]+):((?:[a-z0-9_]+/)*)([a-z0-9_]+)?\$")

        /**
         * Checks if the given [text] is a valid [NodePath].
         */
        @JvmStatic
        fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [NodeParentPath].
         */
        @JvmStatic
        fun parse(value: String): Result<NodeParentPath> {
            if (!isValid(value)) {
                return Result.failure(InvalidNodeParentPathException(value))
            }
            return kotlin.runCatching {
                if (value.endsWith(':')) {
                    ModulePath(value)
                } else if (value.endsWith("/")) {
                    FolderPath(value)
                } else {
                    FolderPath(value)
                }
            }
        }
    }
}
