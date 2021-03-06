package io.github.geeleonidas.withery.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket
import io.github.geeleonidas.withery.registry.WitheryEntityTypes
import io.github.geeleonidas.withery.util.WitheryLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Pair
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.logging.log4j.Level
import kotlin.math.abs

open class SoulEntity(type: EntityType<out SoulEntity>, world: World): Entity(type, world) {
    companion object {
        const val maxVisibleTicks = 20
        const val sideLength = 0.125f
        const val maxVelLenSq = 0.5
        const val maxTargetLenSq = 144
        const val velEpsilon = 5E-4
        const val baseLerpDelta = 0.01
    }

    // State variables
    var remainingVisibleTicks = maxVisibleTicks
        private set
    var forceWandering = false
    var boundEntity: LivingEntity? = null
        set(value) {
            if (value == null) {
                if (this.isBoundTo(field))
                    Withery.log(
                        "Tried to set boundEntity to null without proper sync, please use unbound instead",
                        Level.ERROR
                    )
            } else {
                if (!this.isBoundTo(value))
                    Withery.log(
                        "Tried to set boundEntity to a new value without proper sync, please use boundTo instead",
                        Level.ERROR
                    )
            }

            this.remainingVisibleTicks = maxVisibleTicks
            field = value
        }

    // Random offsets/factors

    var offsetPos: Vec3d = Vec3d(
        this.random.nextDouble() * 0.5 + 0.5,
        this.random.nextDouble() * 0.75,
        this.random.nextDouble() * 0.5 + 0.5
    ).rotateY(this.random.nextFloat() * 360)
    var deltaFactor = 0.75 - this.random.nextDouble() * 0.5

    // Macros

    private val isReadyToAbsorption: Boolean
        get() = !this.forceWandering && this.boundEntity?.hasStatusEffect(StatusEffects.WITHER) == false
    private val LivingEntity.witheryImpl: WitheryLivingEntity
        get() = this as WitheryLivingEntity

    private fun boundTo(livingEntity: LivingEntity) =
        livingEntity.witheryImpl.boundSoul(this)
    private fun unbound() =
        this.boundEntity?.witheryImpl?.unboundSoul(this)
    private fun isBoundTo(livingEntity: LivingEntity?) =
        livingEntity != null && livingEntity.witheryImpl.containsSoul(this)

    // Init

    init { this.noClip = true }

    constructor(world: World, x: Double, y: Double, z: Double): this(WitheryEntityTypes.soulEntity, world) {
        this.updatePosition(x, y, z)
    }

    constructor(boundEntity: LivingEntity): this(WitheryEntityTypes.soulEntity, boundEntity.world) {
        val pos = boundEntity.boundingBox.center
        this.updatePosition(pos.x, pos.y, pos.z)
        this.boundTo(boundEntity)
    }

    constructor(boundEntity: LivingEntity, spawnPacketInfo: Pair<Vec3d, Double>): this(boundEntity) {
        this.offsetPos = spawnPacketInfo.left
        this.deltaFactor = spawnPacketInfo.right
    }

    // Util

    private fun getTargetPos(boundEntity: LivingEntity) =
        if (this.isReadyToAbsorption)
            boundEntity.boundingBox.center
        else
            boundEntity.boundingBox.center.add(offsetPos)

    private fun floorVelocity() {
        var velX = this.velocity.x
        var velY = this.velocity.y
        var velZ = this.velocity.z

        if (abs(velX) < velEpsilon)
            velX = 0.0
        if (abs(velY) < velEpsilon)
            velY = 0.0
        if (abs(velZ) < velEpsilon)
            velZ = 0.0

        this.velocity = Vec3d(velX, velY, velZ)
    }

    // Tick-related functions

    private fun tickMoveToTarget() {
        if (this.boundEntity == null) {
            if (this.velocity.lengthSquared() > 0.0)
                this.velocity = this.velocity.multiply(0.82)
            return
        }

        val boundEntity = this.boundEntity!!

        val thisPos = this.boundingBox.center
        val targetPos = this.getTargetPos(boundEntity)
        val toTarget = targetPos.subtract(thisPos)
        val targetLenSq = toTarget.lengthSquared()

        if (targetLenSq > sideLength * sideLength) {
            if (targetLenSq > maxTargetLenSq) {
                this.teleport(targetPos.x, targetPos.y, targetPos.z)
                this.velocity = Vec3d.ZERO
                return
            }

            val dot = this.velocity.dotProduct(toTarget)
            if (dot < 0 || dot * dot < 0.5 * this.velocity.lengthSquared() * targetLenSq)
                this.velocity = this.velocity.multiply(0.9)

            val delta = if (!this.isReadyToAbsorption)
                this.deltaFactor * baseLerpDelta
            else
                2 * this.deltaFactor * baseLerpDelta
            val velLerp = this.velocity.multiply(1 - delta)
            val toTargetLerp = toTarget.multiply(delta)
            val newVel = velLerp.add(toTargetLerp)

            if (newVel.lengthSquared() < maxVelLenSq)
                this.velocity = newVel
        }
    }

    private fun tickSoulClaim() {
        val aoe = this.boundingBox.expand(2.0)
        val withering = this.world.getEntitiesByClass(LivingEntity::class.java, aoe) { entity ->
            entity.hasStatusEffect(StatusEffects.WITHER) &&
                (entity as WitheryLivingEntity).potentialHealth < entity.maxHealth
        }

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

        this.boundTo(nextBoundEntity)
    }

    private fun tickSoulAbsorption() {
        val boundEntity = this.boundEntity!!

        if (!this.isReadyToAbsorption)
            return

        val aoe = boundEntity.boundingBox.expand(-sideLength.toDouble() * 0.5)
        if (aoe.intersects(this.boundingBox)) {
            boundEntity.heal(1f)
            this.kill() // TODO: Solve the sync issue by creating a S2C Packet
        }
    }

    override fun tick() {
        super.tick()

        if (this.boundEntity != null) {
            if (this.remainingVisibleTicks > 0)
                this.remainingVisibleTicks--
            this.tickSoulAbsorption()
        } else
            this.tickSoulClaim()

        this.tickMoveToTarget()

        this.floorVelocity()
        val newPos = this.pos.add(this.velocity)
        this.updatePosition(newPos.x, newPos.y, newPos.z)
    }

    override fun baseTick() {
        this.world.profiler.push("entityBaseTick")
        this.prevHorizontalSpeed = this.horizontalSpeed
        this.prevPitch = this.pitch
        this.prevYaw = this.yaw

        if (this.y < -64.0)
            this.destroy()

        this.firstUpdate = false
        this.world.profiler.pop()
    }

    // Overrides

    override fun createSpawnPacket(): Packet<*> = SoulSpawnS2CPacket(this)
    override fun readCustomDataFromTag(tag: CompoundTag) = Unit
    override fun writeCustomDataToTag(tag: CompoundTag) = Unit
    override fun initDataTracker() = Unit

    override fun saveToTag(tag: CompoundTag?) =
        if (this.boundEntity == null)
            super.saveToTag(tag)
        else
            false
    override fun moveToWorld(destination: ServerWorld): Entity? = null

    override fun kill() {
        this.unbound()
        this.remainingVisibleTicks = 0
        super.kill()
    }

    override fun destroy() {
        this.unbound()
        this.remainingVisibleTicks = 0
        super.destroy()
    }

    override fun canUsePortals() = false
    override fun isAttackable() = false
    override fun hasNoGravity() = true
    override fun isGlowing() = false
    override fun canClimb() = false
    override fun isOnFire() = false
}