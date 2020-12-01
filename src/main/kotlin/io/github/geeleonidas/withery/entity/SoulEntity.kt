package io.github.geeleonidas.withery.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.world.World

class SoulEntity(type: EntityType<out SoulEntity>, world: World): Entity(type, world) {
    @Environment(EnvType.CLIENT) val offsetX = this.random.nextDouble() * 2.0 - 1.0
    @Environment(EnvType.CLIENT) val offsetY = this.random.nextDouble() * 0.75 + 0.5
    @Environment(EnvType.CLIENT) val offsetZ = this.random.nextDouble() * 2.0 - 1.0

    var boundEntity: LivingEntity? = null
        private set

    constructor(world: World, x: Double, y: Double, z: Double): this(Withery.soulEntityType, world) {
        this.updatePosition(x, y, z)
    }

    constructor(world: World, boundEntity: LivingEntity): this(Withery.soulEntityType, world) {
        this.updatePosition(boundEntity.x, boundEntity.y, boundEntity.z)
        this.boundEntity = boundEntity
    }

    override fun initDataTracker() {  }

    override fun readCustomDataFromTag(tag: CompoundTag) {
        val uuid = tag.getString("BOUND_UUID")
        if (uuid != "")
            boundEntity = null // world.getEntityByUuid(uuid) as LivingEntity?
    }

    override fun writeCustomDataToTag(tag: CompoundTag) {
        tag.putString("BOUND_UUID", boundEntity?.uuidAsString ?: "")
    }

    override fun createSpawnPacket(): Packet<*> = SoulSpawnS2CPacket(this)
}