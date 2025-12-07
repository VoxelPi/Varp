package net.voxelpi.varp.tree.path

public data object RootPath : NodeParentPath {

    override val value: String = "/"

    override val key: String = "/"

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

    override fun div(path: NodePath): NodePath {
        return path
    }

    override fun div(path: NodeParentPath): NodeParentPath {
        return path
    }

    override fun div(path: NodeChildPath): NodeChildPath {
        return path
    }

    override fun div(path: FolderPath): FolderPath {
        return path
    }

    override fun div(path: WarpPath): WarpPath {
        return path
    }

    override fun div(path: RootPath): RootPath {
        return path
    }

    override fun toString(): String {
        return value
    }
}
