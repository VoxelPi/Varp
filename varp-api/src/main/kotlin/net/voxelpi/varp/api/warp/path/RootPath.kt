package net.voxelpi.varp.api.warp.path

data object RootPath : NodeParentPath {

    override val value: String = "/"

    override val allFolders: List<String>
        get() = emptyList()

    override val level: Int
        get() = 0

    override fun isSubPathOf(path: NodePath): Boolean {
        // Root is always a sub path.
        return true
    }

    override fun relativeTo(path: NodeParentPath): RootPath? {
        if (path !is RootPath) {
            return null
        }

        return this
    }

    override fun toString(): String {
        return value
    }
}
