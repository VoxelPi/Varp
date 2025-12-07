package net.voxelpi.varp.serializer.gson

import com.google.gson.GsonBuilder
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeChildPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState

public fun GsonBuilder.varpSerializers(): GsonBuilder {
    registerTypeAdapter(MinecraftLocation::class.java, MinecraftLocationSerializer)

    registerTypeAdapter(WarpState::class.java, WarpStateSerializer)
    registerTypeAdapter(FolderState::class.java, FolderStateSerializer)

    registerTypeAdapter(NodePath::class.java, NodePathSerializer)
    registerTypeAdapter(NodeParentPath::class.java, NodeParentPathSerializer)
    registerTypeAdapter(NodeChildPath::class.java, NodeChildPathSerializer)
    registerTypeAdapter(WarpPath::class.java, WarpPathSerializer)
    registerTypeAdapter(FolderPath::class.java, FolderPathSerializer)
    registerTypeAdapter(RootPath::class.java, RootPathSerializer)

    return this
}
