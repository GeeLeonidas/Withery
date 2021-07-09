package io.github.geeleonidas.withery.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket
import io.github.geeleonidas.withery.registry.WitheryEntityTypes
import io.github.geeleonidas.withery.util.WitheryLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.logging.log4j.Level

open class SoulEntity(type: EntityType<out SoulEntity>, world: World): Entity(type, world) {
    companion object {
        const val sideLength = 0.125f
        const val maxVisibleTicks = 20

        private const val maxToTargetLen = 12.0
        const val maxToTargetLenSq = maxToTargetLen * maxToTargetLen
        private const val velLenEpsilon = 5E-4
        const val velLenSqEpsilon = velLenEpsilon * velLenEpsilon
    }

    // State variables
    var remainingVisibleTicks = maxVisibleTicks
        private set
    var forceWandering = true
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
        this.random.nextDouble() - 0.5,
        this.random.nextDouble() * 0.5 + 0.5
    ).rotateY(this.random.nextFloat() * 360)
    var accFactor = 0.05 * (1.25 - this.random.nextDouble() * 0.5)

    // Macros

    private val isReadyToAbsorption: Boolean
        get() = !this.forceWandering && this.boundEntity?.hasStatusEffect(StatusEffects.WITHER) == false
    private val LivingEntity.moddedLiving: WitheryLivingEntity
        get() = this as WitheryLivingEntity

    private fun boundTo(livingEntity: LivingEntity) =
        livingEntity.moddedLiving.boundSoul(this)
    private fun unbound() =
        this.boundEntity?.moddedLiving?.unboundSoul(this)
    private fun isBoundTo(livingEntity: LivingEntity?) =
        livingEntity != null && livingEntity.moddedLiving.containsSoul(this)

    private fun updatePosition(pos: Vec3d) =
        this.updatePosition(pos.x, pos.y, pos.z)
    private fun addVelocity(delta: Vec3d) =
        this.addVelocity(delta.x, delta.y, delta.z)

    // Init

    init { this.noClip = false }

    constructor(world: World, x: Double, y: Double, z: Double): this(WitheryEntityTypes.soulEntity, world) {
        this.updatePosition(x, y, z)
    }

    constructor(world: World, x: Double, y: Double, z: Double, offsetPos: Vec3d, accFactor: Double): this(world, x, y, z) {
        this.offsetPos = offsetPos
        this.accFactor = accFactor
    }

    constructor(boundEntity: LivingEntity): this(WitheryEntityTypes.soulEntity, boundEntity.world) {
        this.updatePosition(boundEntity.boundingBox.center)
        this.boundTo(boundEntity)
    }

    constructor(boundEntity: LivingEntity, offsetPos: Vec3d, accFactor: Double): this(boundEntity) {
        this.offsetPos = offsetPos
        this.accFactor = accFactor
    }

    // Util

    private fun getTargetPos(boundEntity: LivingEntity): Vec3d {
        val target = boundEntity.pos.add(
            0.0, boundEntity.boundingBox.yLength / 2, 0.0
        )

        return if (this.isReadyToAbsorption)
            target
        else
            target.add(this.offsetPos)
    }

    private fun setVelocityToZero() {
        this.velocity = Vec3d.ZERO
        this.velocityDirty = true
        this.velocityModified = true
    }

    // Tick-related functions

    private fun tickAcceleration() {
        if (this.boundEntity == null)
            return

        val boundEntity = this.boundEntity!!

        val thisBoundingBox = this.boundingBox
        val targetPos = this.getTargetPos(boundEntity)

        if (!thisBoundingBox.contains(targetPos)) {
            val thisPos = thisBoundingBox.center
            val toTarget = targetPos.subtract(thisPos)

            if (toTarget.lengthSquared() > maxToTargetLenSq) {
                this.teleport(targetPos.x, targetPos.y, targetPos.z)
                this.setVelocityToZero()
                return
            }

            val deltaVel = toTarget.multiply(this.accFactor)
            this.addVelocity(deltaVel)
        }
    }

    private fun tickSoulClaim() {
        val aoe = this.boundingBox.expand(2.0)
        val withering = this.world.getEntitiesByClass(LivingEntity::class.java, aoe) {
            entity ->
                entity.hasStatusEffect(StatusEffects.WITHER) &&
                entity.moddedLiving.soulQuantity + entity.health < entity.maxHealth
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

        this.tickAcceleration()
        this.updatePosition(this.pos.add(this.velocity))

        val velLenSq = this.velocity.lengthSquared()
        if (velLenSq > 0) {
            this.addVelocity(this.velocity.multiply(-0.08))
            if (velLenSq < velLenSqEpsilon)
                this.setVelocityToZero()
        }
    }

    override fun baseTick() {
        this.world.profiler.push("entityBaseTick")
        this.prevX = this.x
        this.prevY = this.y
        this.prevZ = this.z

        this.prevHorizontalSpeed = this.horizontalSpeed
        this.prevPitch = this.pitch
        this.prevYaw = this.yaw

        this.velocityDirty = false
        this.velocityModified = false

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
    override fun isLogicalSideForUpdatingMovement() =
        if (this.boundEntity !is PlayerEntity)
            super.isLogicalSideForUpdatingMovement()
        else
            (this.boundEntity as PlayerEntity).isMainPlayer

    override fun kill() {
        if (!this.world.isClient) {
            this.unbound()
            this.world.sendEntityStatus(this, 3)
        }
        super.kill()
    }

    override fun destroy() {
        if (!this.world.isClient) {
            this.unbound()
            this.world.sendEntityStatus(this, 3)
        }
        super.destroy()
    }

    override fun handleStatus(status: Byte) {
        if (status == 3.toByte()) { // onDeath
            this.unbound()
            this.remainingVisibleTicks = 0
        }
        super.handleStatus(status)
    }

    override fun canUsePortals() = false
    override fun isAttackable() = false
    override fun hasNoGravity() = true
    override fun isGlowing() = false
    override fun canClimb() = false
    override fun isOnFire() = false
}