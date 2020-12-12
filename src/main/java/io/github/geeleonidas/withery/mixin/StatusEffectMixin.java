package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {
    @Inject(at = @At("HEAD"), method = "applyUpdateEffect", cancellable = true)
    public void applyUpdateEffect(LivingEntity entity, int amplifier, CallbackInfo ci) {
        boolean isSoulHarvestValid =
            entity.isAlive() && entity.hurtTime == 0 && !entity.isInvulnerable() &&
            entity.world.getRegistryKey() != World.NETHER &&
            this.equals(StatusEffects.WITHER);
        if (isSoulHarvestValid) {
            entity.world.spawnEntity(new SoulEntity(entity));
        }
    }
}
