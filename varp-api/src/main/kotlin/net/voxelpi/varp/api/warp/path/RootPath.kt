package net.voxelpi.varp.api.warp.path

data object RootPath : NodeParentPath {

    override val value: String = "/"

    override val allFolders: List<String>
        get() = emptyList()

    override val level: Int
        get() = 0

    override fun toString(): String {
        return value
    }
}
