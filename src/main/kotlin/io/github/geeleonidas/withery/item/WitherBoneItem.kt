package io.github.geeleonidas.withery.item

import io.github.geeleonidas.withery.registry.entry.WitheryItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class WitherBoneItem: Item(FabricItemSettings()), WitheryItem {
    override val id = "wither_bone"

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val boneItemStack = user.getStackInHand(hand)

        if (user.hasStatusEffect(StatusEffects.WITHER))
            return TypedActionResult.fail(boneItemStack)

        val aof = user.boundingBox.expand(5.0)
        for (entity in world.getEntitiesByClass(LivingEntity::class.java, aof, null))
            entity.addStatusEffect(
                StatusEffectInstance(
                    StatusEffects.WITHER,
                    5 * 20,
                    1
                )
            )
        return TypedActionResult.success(boneItemStack)
    }
}