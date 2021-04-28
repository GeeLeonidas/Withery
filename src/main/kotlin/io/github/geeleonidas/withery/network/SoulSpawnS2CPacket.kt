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
    var offsetX: Double
        private set
    var offsetY: Double
        private set
    var offsetZ: Double
        private set
    var delta: Double
        private set

    init {
        entityId = soulEntity.entityId
        boundId = soulEntity.boundEntity?.entityId ?: -0xDEAD

        x = soulEntity.x
        y = soulEntity.y
        z = soulEntity.z

        offsetX = soulEntity.offsetPos.x
        offsetY = soulEntity.offsetPos.y
        offsetZ = soulEntity.offsetPos.z
        delta = soulEntity.delta
    }

    override fun read(buf: PacketByteBuf) {
        entityId = buf.readInt()
        boundId = buf.readInt()

        x = buf.readDouble()
        y = buf.readDouble()
        z = buf.readDouble()

        offsetX = buf.readDouble()
        offsetY = buf.readDouble()
        offsetZ = buf.readDouble()
        delta = buf.readDouble()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(entityId)
        buf.writeInt(boundId)

        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)

        buf.writeDouble(offsetX)
        buf.writeDouble(offsetY)
        buf.writeDouble(offsetZ)
        buf.writeDouble(delta)
    }

    override fun apply(listener: ClientPlayPacketListener) {
        (listener as WitheryClientPlayPacketListener).onSoulSpawn(this)
    }
}