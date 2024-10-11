package net.voxelpi.varp.warp.path

import net.voxelpi.varp.exception.path.InvalidNodeChildPathException
import java.util.regex.Pattern

public sealed interface NodeChildPath : NodePath {

    /**
     * The id of the node.
     */
    public val id: String

    /**
     * The path to the parent node.
     */
    public val parent: NodeParentPath

    /**
     * A [List] of all parent folders.
     */
    public val parentFolders: List<String>
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return matcher.group(2).removeSuffix("/").split("/")
        }

    override val level: Int
        get() = value.removeSuffix("/").count { it == '/' || it == ':' }

    override fun relativeTo(path: NodeParentPath): NodePath?

    public companion object {
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/(?!\$))*)([a-z0-9_]+)/?\$")

        /**
         * Checks if the given [text] is a valid [NodeChildPath].
         */
        @JvmStatic
        public fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [NodeChildPath].
         */
        @JvmStatic
        public fun parse(value: String): Result<NodeChildPath> {
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
