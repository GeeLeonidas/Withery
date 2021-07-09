package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin {
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Inject(at = @At("HEAD"), method = "applyUpdateEffect", cancellable = true)
    public void applyUpdateEffect(LivingEntity entity, int amplifier, CallbackInfo ci) {
        if (!this.equals(StatusEffects.WITHER))
            return;

        boolean isSoulHarvestValid =
            entity.isAlive() && entity.hurtTime <= 0 &&
            !entity.isInvulnerableTo(DamageSource.WITHER) &&
            entity.world.getRegistryKey() != World.NETHER;

        if (isSoulHarvestValid) {
            if (entity.getHealth() > 1) {
                int entitySoulQuantity = ((WitheryLivingEntity) entity).getSoulQuantity();
                float entityHealth = entity.getHealth();

                List<LivingEntity> weakerEntities = entity.world.getEntitiesByClass(
                    LivingEntity.class,
                    entity.getBoundingBox().expand(4),
                    it ->
                        it.getHealth() < entityHealth &&
                        it.hasStatusEffect(StatusEffects.WITHER)
                );

                for (LivingEntity otherEntity : weakerEntities) {
                    float otherEnergy =
                        otherEntity.getHealth() + ((WitheryLivingEntity) otherEntity).getSoulQuantity();
                    if (otherEntity.getMaxHealth() - otherEnergy > entitySoulQuantity) {
                        entity.world.spawnEntity(new SoulEntity(entity));
                        return; // Prevents applyUpdateEffect from being cancelled
                    }
                }
            }

            ci.cancel();
        }
    }
}
