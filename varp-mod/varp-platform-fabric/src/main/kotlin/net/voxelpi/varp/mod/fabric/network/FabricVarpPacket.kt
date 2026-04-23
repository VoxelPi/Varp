package net.voxelpi.varp.mod.fabric.network

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.fabric.util.toIdentifier
import java.nio.charset.StandardCharsets

data class FabricVarpPacket(
    val payload: String,
) : CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<FabricVarpPacket> = PACKET_ID

    fun encode(buffer: FriendlyByteBuf) {
        buffer.writeBytes(payload.toByteArray(StandardCharsets.UTF_8))
    }

    companion object {

        fun decode(buffer: FriendlyByteBuf): FabricVarpPacket {
            val readableBytes = buffer.readableBytes()
            check(readableBytes > 0) { "Received invalid varp packet. Packet size is less or equal 0." }

            val payload = buffer.toString(StandardCharsets.UTF_8)
            buffer.readerIndex(buffer.readerIndex() + buffer.readableBytes())

            return FabricVarpPacket(payload)
        }

        val PACKET_ID = CustomPacketPayload.Type<FabricVarpPacket>(VarpModConstants.VARP_PLUGIN_CHANNEL.toIdentifier())

        val PACKET_CODEC: StreamCodec<RegistryFriendlyByteBuf, FabricVarpPacket> = CustomPacketPayload.codec(FabricVarpPacket::encode, FabricVarpPacket::decode)
    }
}
