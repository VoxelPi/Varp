package net.voxelpi.varp.option

/**
 * An option that controls how a varp action is performed.
 */
@JvmRecord
public data class Option<T>(val default: T)
