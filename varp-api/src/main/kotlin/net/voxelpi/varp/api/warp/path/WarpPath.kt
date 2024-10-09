package net.voxelpi.varp.api.warp.path

import net.voxelpi.varp.api.exception.path.InvalidWarpPathException
import java.util.regex.Pattern

@JvmRecord
data class WarpPath(
    override val value: String,
) : NodePath, NodeChildPath {

    init {
        if (!isValid(value)) {
            throw InvalidWarpPathException(value)
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
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/)*)([a-z0-9_]+)\$")

        /**
         * Checks if the given [text] is a valid [WarpPath].
         */
        @JvmStatic
        fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [WarpPath].
         */
        @JvmStatic
        fun parse(path: String): Result<WarpPath> {
            return kotlin.runCatching { WarpPath(path) }
        }

        @JvmStatic
        fun build(module: String, vararg folders: String, warp: String): FolderPath {
            return FolderPath("$module:${folders.joinToString("/")}/$warp")
        }
    }
}
