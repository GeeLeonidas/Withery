package io.github.geeleonidas.withery.network

import io.github.geeleonidas.withery.entity.SoulEntity
import io.github.geeleonidas.withery.util.WitheryClientPlayPacketListener
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.listener.ClientPlayPacketListener

class SoulSpawnS2CPacket(soulEntity: SoulEntity): Packet<ClientPlayPacketListener> {
    var entityId: Int
        private set
    var boundId: Int
        private set
    var x: Double
        private set
    var y: Double
        private set
    var z: Double
        private set

    init {
        entityId = soulEntity.entityId
        boundId = soulEntity.boundEntityId
        x = soulEntity.x
        y = soulEntity.y
        z = soulEntity.z
    }

    override fun read(buf: PacketByteBuf) {
        entityId = buf.readInt()
        boundId = buf.readInt()
        x = buf.readDouble()
        y = buf.readDouble()
        z = buf.readDouble()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(entityId)
        buf.writeInt(boundId)
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
    }

    override fun apply(listener: ClientPlayPacketListener) {
        (listener as WitheryClientPlayPacketListener).onSoulSpawn(this)
    }
}