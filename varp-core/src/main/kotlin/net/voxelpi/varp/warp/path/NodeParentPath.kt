package net.voxelpi.varp.warp.path

import net.voxelpi.varp.exception.path.InvalidNodeParentPathException
import java.util.regex.Pattern

public sealed interface NodeParentPath : NodePath {

    /**
     * Returns a list of all folders including the target folder if this is a folder path.
     */
    public val allFolders: List<String>
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return matcher.group(2).removeSuffix("/").split("/")
        }

    /**
     * Creates a [WarpPath] to the warp in the parent node referenced by this path with the name [id].
     */
    public fun warp(id: String): WarpPath {
        return WarpPath("$value$id")
    }

    /**
     * Creates a [FolderPath] to the folder in the parent node referenced by this path with the name [id].
     */
    public fun folder(id: String): FolderPath {
        return FolderPath("$value$id/")
    }

    /**
     * Returns if this path is part of or equal to the given [path].
     */
    public fun isSubPathOf(path: NodePath): Boolean {
        return path.value.startsWith(value)
    }

    /**
     * Returns if this path is part of but not equal to the given [path].
     */
    public fun isTrueSubPathOf(path: NodePath): Boolean {
        return isSubPathOf(path) && path != this
    }

    override fun relativeTo(path: NodeParentPath): NodeParentPath?

    public companion object {
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/)*)\$")

        /**
         * Checks if the given [text] is a valid [NodeParentPath].
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
            return runCatching {
                if (value == "/") {
                    RootPath
                } else {
                    FolderPath(value)
                }
            }
        }

        @JvmStatic
        public fun build(vararg folders: String): NodeParentPath {
            return if (folders.isEmpty()) {
                RootPath
            } else {
                FolderPath("/${folders.joinToString("/")}")
            }
        }
    }
}
