package io.github.geeleonidas.withery.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket
import io.github.geeleonidas.withery.util.WitheryLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

open class SoulEntity(type: EntityType<out SoulEntity>, world: World): Entity(type, world) {
    var boundEntity: LivingEntity? = null
        protected set(value) {
            (value as WitheryLivingEntity?)?.boundSoul(this)
            field = value
        }

    fun unbound() {
        this.boundEntity = null
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

    private fun tickMovement() {
        val boundEntity = this.boundEntity ?: return

        if (boundEntity.isDead || boundEntity.removed)
            return

        val thisPos = this.boundingBox.center
        val targetPos = boundEntity.boundingBox.center
        val toTarget = targetPos.subtract(thisPos)
        val targetLenSq = toTarget.lengthSquared()

        if (targetLenSq > 0.125 * 0.125) {
            if (targetLenSq > 100) {
                this.updatePosition(targetPos.x, targetPos.y, targetPos.z)
                this.velocity = Vec3d.ZERO
                return
            }

            this.velocity =
                this.velocity.add(
                    toTarget.normalize().multiply(targetLenSq * 0.002)
                )

            val dot = toTarget.dotProduct(this.velocity)

            if (targetLenSq < 0.5)
                this.velocity =
                    this.velocity.multiply(0.8)
            else if (dot < 0 || dot * dot / this.velocity.lengthSquared() < 0.5 * targetLenSq)
                this.velocity =
                    this.velocity.multiply(0.9).add(
                        toTarget.normalize().multiply(targetLenSq * 0.01)
                    )
        }
    }

    private fun tickBoundEntity() {
        val boundEntity = this.boundEntity

        if (boundEntity == null) {
            val aoe = this.boundingBox.expand(2.0)
            val withering = this.world.getEntitiesByClass(LivingEntity::class.java, aoe)
                { entity -> entity.hasStatusEffect(StatusEffects.WITHER) }
            if (withering.isEmpty())
                return

            var nextBoundEntity = withering.removeAt(0)
            var lowestDist = nextBoundEntity.pos.squaredDistanceTo(this.pos)
            for (entity in withering) {
                val currentDist = entity.pos.squaredDistanceTo(this.pos)
                if (currentDist < lowestDist) {
                    nextBoundEntity = entity
                    lowestDist = currentDist
                }
            }

            this.boundEntity = nextBoundEntity
        } else {
            if (boundEntity.isDead || boundEntity.removed)
                return
        }
    }

    override fun tick() {
        super.tick()

        this.prevX = this.x
        this.prevY = this.y
        this.prevZ = this.z

        this.tickMovement()

        this.move(MovementType.SELF, this.velocity)

        this.tickBoundEntity()
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