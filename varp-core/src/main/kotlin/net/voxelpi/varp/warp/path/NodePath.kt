package net.voxelpi.varp.warp.path

import net.voxelpi.varp.exception.path.InvalidNodeParentPathException
import java.util.regex.Pattern

/**
 * A path to a node in the warp node tree.
 */
public sealed interface NodePath {

    /**
     * The path of the node as string.
     */
    public val value: String

    /**
     * The level of the node.
     */
    public val level: Int

    /**
     * Returns this path as if the given [path] is the root path.
     * If the given [path] is not a parent of this path, null is returned.
     */
    public fun relativeTo(path: NodeParentPath): NodePath?

    public companion object {
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/)*)([a-z0-9_]+)?\$")

        /**
         * Checks if the given [text] is a valid [NodePath].
         */
        @JvmStatic
        public fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [NodeParentPath].
         */
        @JvmStatic
        public fun parse(value: String): Result<NodeParentPath> {
            if (!isValid(value)) {
                return Result.failure(InvalidNodeParentPathException(value))
            }
            return kotlin.runCatching {
                if (value == "/") {
                    RootPath
                } else if (value.endsWith("/")) {
                    FolderPath(value)
                } else {
                    FolderPath(value)
                }
            }
        }
    }
}
