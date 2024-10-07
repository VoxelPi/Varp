package net.voxelpi.varp.api.warp.path

import net.voxelpi.varp.api.warp.exception.path.InvalidModulePathException
import java.util.regex.Pattern

@JvmRecord
data class ModulePath(
    override val value: String,
) : NodeParentPath {

    init {
        if (!isValid(value)) {
            throw InvalidModulePathException(value)
        }
    }

    override val id: String
        get() = value.substring(0 until (value.length - 1))

    override val module: ModulePath
        get() = this

    override val allFolders: List<String>
        get() = emptyList()

    override val level: Int
        get() = 0

    override fun toString(): String {
        return value
    }

    companion object {
        private val PATH_PATTERN = Pattern.compile("^([a-z0-9_]+):")

        /**
         * Checks if the given [text] is a valid [ModulePath].
         */
        @JvmStatic
        fun isValid(text: String): Boolean {
            return PATH_PATTERN.matcher(text).matches()
        }

        /**
         * Parses the given string into a [ModulePath].
         */
        @JvmStatic
        fun parse(value: String): Result<ModulePath> {
            return runCatching { ModulePath(value) }
        }

        /**
         * Creates a [ModulePath] to the module with the given [id].
         */
        @JvmStatic
        fun module(id: String): ModulePath {
            return ModulePath("$id:")
        }
    }
}
