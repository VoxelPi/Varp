package net.voxelpi.varp.util

@JvmRecord
public data class Movement<T>(
    public val from: T,
    public val to: T,
)
