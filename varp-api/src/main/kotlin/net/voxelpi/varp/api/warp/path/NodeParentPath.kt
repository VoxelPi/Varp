package net.voxelpi.varp.api.warp.path

import net.voxelpi.varp.api.exception.path.InvalidNodeParentPathException
import java.util.regex.Pattern

sealed interface NodeParentPath : NodePath {

    /**
     * Returns a list of all folders including the target folder if this is a folder path.
     */
    val allFolders: List<String>
        get() {
            val matcher = PATH_PATTERN.matcher(value)
            check(matcher.find())
            return matcher.group(2).removeSuffix("/").split("/")
        }

    /**
     * Creates a [WarpPath] to the warp in the parent node referenced by this path with the name [id].
     */
    fun warp(id: String): WarpPath {
        return WarpPath("$value$id")
    }

    /**
     * Creates a [FolderPath] to the folder in the parent node referenced by this path with the name [id].
     */
    fun folder(id: String): FolderPath {
        return FolderPath("$value$id/")
    }

    /**
     * Returns if the parent contains the child at the given [childPath].
     */
    fun contains(childPath: NodeChildPath): Boolean {
        return childPath.parent.value.startsWith(this.value)
    }

    companion object {
        private val PATH_PATTERN = Pattern.compile("^/((?:[a-z0-9_]+/)*)\$")

        /**
         * Checks if the given [text] is a valid [NodeParentPath].
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
                if (value == "/") {
                    RootPath
                } else {
                    FolderPath(value)
                }
            }
        }

        @JvmStatic
        fun build(module: String, vararg folders: String): NodeParentPath {
            return if (folders.isEmpty()) {
                RootPath
            } else {
                FolderPath("/${folders.joinToString("/")}")
            }
        }
    }
}
