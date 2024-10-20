package net.voxelpi.varp.mod.network.protocol

/**
 * Annotation to specify the id of a varp packet.
 */
@Target(AnnotationTarget.CLASS)
annotation class PacketId(val id: String)
