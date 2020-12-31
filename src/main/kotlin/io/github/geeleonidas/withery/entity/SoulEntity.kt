package io.github.geeleonidas.withery.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket
import io.github.geeleonidas.withery.util.WitheryLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

open class SoulEntity(type: EntityType<out SoulEntity>, world: World): Entity(type, world) {
    protected var offsetAngle = 0.0
    protected var offsetY = 0.0
    protected var radius = 0.0
    var boundEntity: LivingEntity? = null
        protected set(value) {
            if (value != null) {
                (value as WitheryLivingEntity).boundSoul(this)
                offsetY = this.random.nextDouble() // TODO: Create factors and ranges for these
                radius = this.random.nextDouble()
            }
            field = value
        }

    init { this.noClip = true }

    constructor(world: World, x: Double, y: Double, z: Double): this(Withery.soulEntityType, world) {
        this.updatePosition(x, y, z)
    }

    constructor(boundEntity: LivingEntity): this(Withery.soulEntityType, boundEntity.world) {
        val pos = boundEntity.boundingBox.center
        this.updatePosition(pos.x, pos.y, pos.z)
        this.boundEntity = boundEntity
    }

    fun unbound() { this.boundEntity = null }

    protected fun tickBoundEntity(boundEntity: LivingEntity?) {
        if (boundEntity == null)
            return
        if (boundEntity.isDead || boundEntity.removed)
            return

        val thisPos = this.boundingBox.center
        val targetPos = boundEntity.boundingBox.center
        val distToTarget = targetPos.distanceTo(thisPos)

        if (distToTarget > 0.125)
            if (distToTarget < 10)
                this.updatePosition(
                    MathHelper.lerp(0.2, thisPos.x, targetPos.x),
                    MathHelper.lerp(0.2, thisPos.y, targetPos.y),
                    MathHelper.lerp(0.2, thisPos.z, targetPos.z)
                )
            else
                this.updatePosition(targetPos.x, targetPos.y, targetPos.z)
    }

    override fun tick() {
        this.tickBoundEntity(this.boundEntity)
        super.tick()
    }

    override fun createSpawnPacket(): Packet<*> = SoulSpawnS2CPacket(this)
    override fun readCustomDataFromTag(tag: CompoundTag) = Unit
    override fun writeCustomDataToTag(tag: CompoundTag) = Unit
    override fun initDataTracker() = Unit

    override fun saveToTag(tag: CompoundTag?) =
        if (boundEntity == null)
            super.saveToTag(tag)
        else
            false
    override fun moveToWorld(destination: ServerWorld): Entity? = null

    override fun kill() {
        (this.boundEntity as WitheryLivingEntity?)?.unboundSoul(this)
        super.kill()
    }

    override fun canUsePortals() = false
    override fun isAttackable() = false
    override fun hasNoGravity() = true
    override fun canClimb() = false
    override fun isOnFire() = false
}