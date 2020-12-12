package io.github.geeleonidas.withery.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket
import io.github.geeleonidas.withery.util.WitheryServerWorld
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import java.util.*

class SoulEntity(type: EntityType<out SoulEntity>, world: World): Entity(type, world) {
    var boundEntityUuid: UUID? = null
    var boundEntityId = -0xDEAD
    var boundEntity: LivingEntity?
        get() = world.getEntityById(boundEntityId) as LivingEntity?
        set(value) {
            if (value != null) {
                boundEntityId = value.entityId
                boundEntityUuid = value.uuid
            } else {
                boundEntityId = -0xDEAD
                boundEntityUuid = null
            }
        }

    constructor(world: World, x: Double, y: Double, z: Double): this(Withery.soulEntityType, world) {
        this.updatePosition(x, y, z)
    }

    constructor(boundEntity: LivingEntity): this(Withery.soulEntityType, boundEntity.world) {
        this.updatePosition(boundEntity.x, boundEntity.y, boundEntity.z)
        this.boundEntity = boundEntity
    }

    override fun tick() {
        if (this.age % 60 == 0)
            Withery.log(boundEntity?.entityId ?: "No entityId")
        super.tick()
    }

    override fun initDataTracker() = Unit

    override fun readCustomDataFromTag(tag: CompoundTag) {
        if (!world.isClient && tag.contains("bound_uuid"))
            boundEntityUuid = tag.getUuid("bound_uuid")
    }

    override fun writeCustomDataToTag(tag: CompoundTag) {
        if (!world.isClient && boundEntityUuid != null)
            tag.putUuid("bound_uuid", boundEntityUuid!!)
    }

    override fun createSpawnPacket(): Packet<*> {
        if (boundEntityUuid != null)
            boundEntityId = (world as WitheryServerWorld).getEntityByUuid(boundEntityUuid)?.entityId ?: -0xDEAD
        return SoulSpawnS2CPacket(this)
    }
}