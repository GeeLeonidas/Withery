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
    @Environment(EnvType.CLIENT) val offsetX = this.random.nextDouble() * 2.0 - 1.0
    @Environment(EnvType.CLIENT) val offsetY = this.random.nextDouble() * 0.75 + 0.5
    @Environment(EnvType.CLIENT) val offsetZ = this.random.nextDouble() * 2.0 - 1.0

    companion object {
        val trackedBoundEntityID: TrackedData<Int> =
            DataTracker.registerData(SoulEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
    }

    var boundEntity: LivingEntity? = null
        set(value) {
            dataTracker.set(trackedBoundEntityID, value?.entityId ?: -0xDEAD)
            field = value
        }
    val boundEntityId: Int
        get() = dataTracker.get(trackedBoundEntityID)

    constructor(world: World, x: Double, y: Double, z: Double): this(Withery.soulEntityType, world) {
        this.updatePosition(x, y, z)
    }

    constructor(world: World, boundEntity: LivingEntity): this(Withery.soulEntityType, world) {
        this.updatePosition(boundEntity.x, boundEntity.y, boundEntity.z)
        this.boundEntity = boundEntity
    }

    override fun tick() {
        if (this.age % 20 == 0)
            Withery.log(boundEntity?.uuidAsString ?: "No UUID")
        super.tick()
    }

    override fun initDataTracker() {
        dataTracker.startTracking(trackedBoundEntityID, -0xDEAD)
    }

    override fun readCustomDataFromTag(tag: CompoundTag) {
        if (tag.contains("bound_uuid")) {
            if (!world.isClient) {
                Withery.log("AA")
                val uuid = tag.getUuid("bound_uuid")
                boundEntity = (world as WitheryServerWorld).getEntityByUuid(uuid) as LivingEntity?
            }
            else
                boundEntity = world.getEntityById(boundEntityId) as LivingEntity?
        }
    }

    override fun writeCustomDataToTag(tag: CompoundTag) {
        if (boundEntity != null)
            tag.putUuid("bound_uuid", boundEntity!!.uuid)
    }

    override fun createSpawnPacket(): Packet<*> = SoulSpawnS2CPacket(this)
}