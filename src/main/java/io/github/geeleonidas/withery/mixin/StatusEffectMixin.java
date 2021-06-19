package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.Withery;
import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
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
                int soulQuantity = ((WitheryLivingEntity) entity).getSoulQuantity();
                float soulEnergy = soulQuantity + entity.getHealth();

                List<LivingEntity> weakerEntities = entity.world.getEntitiesByClass(
                    LivingEntity.class,
                    entity.getBoundingBox().expand(4),
                    e -> e.hasStatusEffect(StatusEffects.WITHER) &&
                         e.getMaxHealth() - e.getHealth() > soulQuantity &&
                        ((WitheryLivingEntity) e).getSoulQuantity() + e.getHealth() < soulEnergy
                );

                if (!weakerEntities.isEmpty()) {
                    entity.world.spawnEntity(new SoulEntity(entity));
                    return; // Prevents applyUpdateEffect from being cancelled
                }
            }

            ci.cancel();
        }
    }
}
