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
import net.minecraft.util.Pair
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.logging.log4j.Level

open class SoulEntity(type: EntityType<out SoulEntity>, world: World): Entity(type, world) {
    companion object {
        const val maxVisibleTicks = 20
        const val sideLength = 0.125f
        const val maxVelLenSq = 0.5
    }

    var remainingVisibleTicks = maxVisibleTicks
        private set
    var isGoingToBeAbsorbed = false
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
            this.isGoingToBeAbsorbed = false
            field = value
        }

    private fun boundTo(livingEntity: LivingEntity) =
        (livingEntity as WitheryLivingEntity).boundSoul(this)
    private fun unbound() =
        (this.boundEntity as WitheryLivingEntity?)?.unboundSoul(this)
    private fun isBoundTo(livingEntity: LivingEntity?) =
        (livingEntity as WitheryLivingEntity?)?.containsSoul(this) ?: false

    var offsetPos: Vec3d = Vec3d(
        this.random.nextDouble() * 0.5 + 0.5,
        this.random.nextDouble() * 0.75,
        this.random.nextDouble() * 0.5 + 0.5
    ).rotateY(this.random.nextFloat() * 360)
    var accFactor =
        0.002 * (0.75 - this.random.nextDouble() * 0.5)

    init { this.noClip = true }

    constructor(world: World, x: Double, y: Double, z: Double): this(Withery.soulEntityType, world) {
        this.updatePosition(x, y, z)
    }

    constructor(boundEntity: LivingEntity): this(Withery.soulEntityType, boundEntity.world) {
        val pos = boundEntity.boundingBox.center
        this.updatePosition(pos.x, pos.y, pos.z)
        this.boundTo(boundEntity)
    }

    constructor(boundEntity: LivingEntity, spawnPacketInfo: Pair<Vec3d, Double>): this(boundEntity) {
        this.offsetPos = spawnPacketInfo.left
        this.accFactor = spawnPacketInfo.right
    }

    private fun getTargetPos(boundEntity: LivingEntity) =
        if (isGoingToBeAbsorbed)
            boundEntity.boundingBox.center
        else
            boundEntity.boundingBox.center.add(offsetPos)

    private fun tickMovement() {
        if (this.boundEntity == null) {
            if (this.velocity.lengthSquared() > sideLength * sideLength) {
                this.velocity = this.velocity.multiply(0.9)
                this.velocityModified = true
                this.velocityDirty = true
            }
            return
        }

        val boundEntity = this.boundEntity!!

        val thisPos = this.boundingBox.center
        val targetPos = this.getTargetPos(boundEntity)
        val toTarget = targetPos.subtract(thisPos)
        val targetLenSq = toTarget.lengthSquared()

        if (targetLenSq > sideLength * sideLength) {
            this.velocityModified = true
            this.velocityDirty = true

            if (targetLenSq > 144) {
                this.teleport(targetPos.x, targetPos.y, targetPos.z)
                this.velocity = Vec3d.ZERO
                return
            }

            val dot = toTarget.dotProduct(this.velocity)

            var factor = this.accFactor
            if (dot < 0 || dot * dot / this.velocity.lengthSquared() < 0.5 * targetLenSq) {
                this.velocity =
                    this.velocity.multiply(0.95)
                factor *= 2
            }

            val velLenSq = this.velocity.lengthSquared()
            val accLen = factor * targetLenSq

            if (velLenSq + accLen < maxVelLenSq)
                this.velocity =
                    this.velocity.add(toTarget.normalize().multiply(accLen))
        }

        if (this.remainingVisibleTicks > 0)
            this.remainingVisibleTicks--
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

        val aoe = boundEntity.boundingBox.expand(-sideLength.toDouble())
        if (aoe.intersects(this.boundingBox)) {
            boundEntity.heal(1f)
            this.kill()
        }
    }

    override fun tick() {
        super.tick()

        this.prevX = this.x
        this.prevY = this.y
        this.prevZ = this.z

        if (this.boundEntity == null)
            this.tickSoulClaim()
        else if (this.isGoingToBeAbsorbed)
            this.tickSoulAbsorption()

        this.tickMovement()

        this.move(MovementType.SELF, this.velocity)
    }

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
        super.kill()
    }

    override fun canUsePortals() = false
    override fun isAttackable() = false
    override fun hasNoGravity() = true
    override fun canClimb() = false
    override fun isOnFire() = false
}