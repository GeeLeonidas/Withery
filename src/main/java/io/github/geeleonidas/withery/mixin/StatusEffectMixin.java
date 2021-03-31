package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin {
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Inject(at = @At("HEAD"), method = "applyUpdateEffect", cancellable = true)
    public void applyUpdateEffect(LivingEntity entity, int amplifier, CallbackInfo ci) {
        boolean isSoulHarvestValid =
            entity.isAlive() && entity.hurtTime <= 0 &&
            !entity.isInvulnerableTo(DamageSource.WITHER) &&
            entity.world.getRegistryKey() != World.NETHER;

        if (isSoulHarvestValid && this.equals(StatusEffects.WITHER)) {
            if (entity.getHealth() > 1) {
                Box aoe = entity.getBoundingBox().expand(4);
                List<LivingEntity> allLiving = entity.world.getEntitiesByClass(LivingEntity.class, aoe, null);

                for (Entity otherEntity : allLiving) {
                    float otherEntityPotHealth = ((WitheryLivingEntity) otherEntity).getPotentialHealth();
                    float entityPotHealth = ((WitheryLivingEntity) entity).getPotentialHealth();

                    if (otherEntityPotHealth < entityPotHealth) {
                        entity.world.spawnEntity(new SoulEntity(entity));
                        return; // Prevents applyUpdateEffect from being cancelled
                    }
                }
            }

            ci.cancel();
        }
    }
}
