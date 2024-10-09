package net.voxelpi.varp.api.warp.path

import net.voxelpi.varp.api.exception.path.InvalidNodeChildPathException
import java.util.regex.Pattern

sealed interface NodeChildPath : NodePath {

    /**
     * The id of the node.
     */
    val id: String

    /**
     * The path to the parent node.
     */
    val parent: NodeParentPath

    /**
     * A [List] of all parent folders.
     */
    val parentFolders: List<String>
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return matcher.group(2).removeSuffix("/").split("/")
        }

    /**
     * Returns if the container of the given [parentPath] contains the child.
     */
    fun containedBy(parentPath: NodeParentPath): Boolean {
        return parentPath.contains(this)
    }

    override val level: Int
        get() = value.removeSuffix("/").count { it == '/' || it == ':' }

    companion object {
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/(?!\$))*)([a-z0-9_]+)/?\$")

        /**
         * Checks if the given [text] is a valid [NodeChildPath].
         */
        @JvmStatic
        fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [NodeChildPath].
         */
        @JvmStatic
        fun parse(value: String): Result<NodeChildPath> {
            if (!isValid(value)) {
                return Result.failure(InvalidNodeChildPathException(value))
            }
            return kotlin.runCatching {
                if (value.endsWith('/')) {
                    FolderPath(value)
                } else {
                    WarpPath(value)
                }
            }
        }
    }
}
