package net.voxelpi.varp.serializer.gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal inline fun <reified T> typeOf(): Type {
    return object : TypeToken<T>() {}.type
}
