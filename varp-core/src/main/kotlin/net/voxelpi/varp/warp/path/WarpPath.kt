package net.voxelpi.varp.warp.path

import net.voxelpi.varp.exception.path.InvalidWarpPathException
import java.util.regex.Pattern

@JvmRecord
public data class WarpPath(
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
            return matcher.group(2)
        }

    override val key: String
        get() = id

    override val parent: NodeParentPath
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return NodeParentPath.parse("/${matcher.group(1)}").getOrThrow()
        }

    override fun relativeTo(path: NodeParentPath): WarpPath? {
        if (!path.isSubPathOf(this)) {
            return null
        }

        // Remove the path prefix from this value (but keep the slash).
        return WarpPath(this.value.substring(path.value.length - 1))
    }

    override fun toString(): String {
        return value
    }

    public companion object {
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/)*)([a-z0-9_]+)\$")

        /**
         * Checks if the given [text] is a valid [WarpPath].
         */
        @JvmStatic
        public fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [WarpPath].
         */
        @JvmStatic
        public fun parse(path: String): Result<WarpPath> {
            return runCatching { WarpPath(path) }
        }

        @JvmStatic
        public fun build(module: String, vararg folders: String, warp: String): FolderPath {
            return FolderPath("$module:${folders.joinToString("/")}/$warp")
        }
    }
}
