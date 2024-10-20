package net.voxelpi.varp.mod.fabric.network

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.fabric.util.toIdentifier
import java.nio.charset.StandardCharsets

data class FabricVarpPacket(
    val payload: String,
) : CustomPayload {

    override fun getId(): CustomPayload.Id<FabricVarpPacket> = PACKET_ID

    fun encode(buffer: PacketByteBuf) {
        buffer.writeBytes(payload.toByteArray(StandardCharsets.UTF_8))
    }

    companion object {

        fun decode(buffer: PacketByteBuf): FabricVarpPacket {
            val readableBytes = buffer.readableBytes()
            check(readableBytes > 0) { "Received invalid varp packet. Packet size is less or equal 0." }

            val payload = buffer.toString(StandardCharsets.UTF_8)
            buffer.readerIndex(buffer.readerIndex() + buffer.readableBytes())

            return FabricVarpPacket(payload)
        }

        val PACKET_ID = CustomPayload.Id<FabricVarpPacket>(VarpModConstants.VARP_PLUGIN_CHANNEL.toIdentifier())

        val PACKET_CODEC: PacketCodec<RegistryByteBuf, FabricVarpPacket> = CustomPayload.codecOf(FabricVarpPacket::encode, FabricVarpPacket::decode)
    }
}
