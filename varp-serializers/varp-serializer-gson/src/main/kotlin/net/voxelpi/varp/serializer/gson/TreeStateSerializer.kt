package net.voxelpi.varp.serializer.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import java.lang.reflect.Type

public object TreeStateSerializer : JsonSerializer<TreeState>, JsonDeserializer<TreeState> {

    override fun serialize(src: TreeState, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonObject().apply {
            add("warps", context.serialize(src.warps, typeOf<Map<WarpPath, WarpState>>()))
            add("folders", context.serialize(src.folders, typeOf<Map<FolderPath, FolderState>>()))
            add("root", context.serialize(src.root, typeOf<FolderState>()))
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TreeState {
        require(json is JsonObject)
        val root = context.deserialize<FolderState>(json["root"], FolderState::class.java)
        val warps = context.deserialize<Map<WarpPath, WarpState>>(json["warps"], typeOf<Map<WarpPath, WarpState>>())
        val folders = context.deserialize<Map<FolderPath, FolderState>>(json["folders"], typeOf<Map<FolderPath, FolderState>>())
        return MutableTreeState(warps.toMutableMap(), folders.toMutableMap(), root)
    }
}
