package net.voxelpi.varp.api.warp.path

import net.voxelpi.varp.api.warp.exception.path.InvalidFolderPathException
import java.util.regex.Pattern

@JvmRecord
data class FolderPath(
    override val value: String,
) : NodeParentPath, NodeChildPath {

    init {
        if (!isValid(value)) {
            throw InvalidFolderPathException(value)
        }
    }

    override val id: String
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return matcher.group(3)
        }

    override val parent: NodeParentPath
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return NodeParentPath.parse("${matcher.group(1)}:${matcher.group(2)}").getOrThrow()
        }

    override fun toString(): String {
        return value
    }

    companion object {
        private val PATH_PATTERN = Pattern.compile("^([a-z0-9_]+):((?:[a-z0-9_]+/(?!$))*)([a-z0-9_]+)/$")

        /**
         * Checks if the given [text] is a valid [WarpPath].
         */
        @JvmStatic
        fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [FolderPath].
         */
        @JvmStatic
        fun parse(value: String): Result<FolderPath> {
            return runCatching { FolderPath(value) }
        }

        @JvmStatic
        fun build(module: String, folder1: String, vararg folders: String): FolderPath {
            return FolderPath("$module:$folder1/${folders.joinToString("/")}")
        }
    }
}
