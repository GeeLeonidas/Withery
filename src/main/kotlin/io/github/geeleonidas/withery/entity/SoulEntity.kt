package io.github.geeleonidas.withery.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket
import io.github.geeleonidas.withery.util.WitheryLivingEntity
import io.github.geeleonidas.withery.util.WitheryServerWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
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
                (value as WitheryLivingEntity).claimSoul(this)
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
        super.tick()
    }

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
            boundEntity = (world as WitheryServerWorld).getLivingEntityByUuid(boundEntityUuid)
        return SoulSpawnS2CPacket(this)
    }

    override fun initDataTracker() = Unit
    override fun moveToWorld(destination: ServerWorld): Entity? = null

    override fun canUsePortals() = false
    override fun doesRenderOnFire() = false
}