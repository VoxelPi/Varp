package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import org.spongepowered.configurate.serialize.TypeSerializerCollection

public object VarpConfigurateSerializers {

    public val serializers: TypeSerializerCollection = TypeSerializerCollection.defaults().childBuilder().apply {
        register(MinecraftLocation::class.java, MinecraftLocationSerializer)

        register(WarpState::class.java, WarpStateSerializer)
        register(FolderState::class.java, FolderStateSerializer)

        register(NodePathSerializer)
        register(NodeParentPathSerializer)
        register(NodeChildPathSerializer)
        register(WarpPathSerializer)
        register(FolderPathSerializer)
        register(RootPathSerializer)
    }.build()
}
