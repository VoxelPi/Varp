package net.voxelpi.varp.warp.path

import net.voxelpi.varp.exception.path.InvalidFolderPathException
import java.util.regex.Pattern

@JvmRecord
public data class FolderPath(
    override val value: String,
) : NodeParentPath, NodeChildPath {

    override val key: String
        get() = "$id/"

    init {
        if (!isValid(value)) {
            throw InvalidFolderPathException(value)
        }
    }

    override val id: String
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return matcher.group(2)
        }

    override val parent: NodeParentPath
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return NodeParentPath.parse("/${matcher.group(1)}").getOrThrow()
        }

    override fun relativeTo(path: NodeParentPath): NodeParentPath? {
        if (!path.isSubPathOf(this)) {
            return null
        }

        // Remove the path prefix from this value (but keep the slash).
        return NodeParentPath.parse(this.value.substring(path.value.length - 1)).getOrThrow()
    }

    override fun toString(): String {
        return value
    }

    public companion object {
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/(?!\$))*)([a-z0-9_]+)/\$")

        /**
         * Checks if the given [text] is a valid [WarpPath].
         */
        @JvmStatic
        public fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [FolderPath].
         */
        @JvmStatic
        public fun parse(value: String): Result<FolderPath> {
            return runCatching { FolderPath(value) }
        }

        @JvmStatic
        public fun build(module: String, folder1: String, vararg folders: String): FolderPath {
            return FolderPath("$module:$folder1/${folders.joinToString("/")}")
        }
    }
}
