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
            int entitySoulQuantity = ((WitheryLivingEntity) entity).getSoulQuantity();
            float entityHealth = entity.getHealth();

            List<LivingEntity> weakerEntities = entity.world.getEntitiesByClass(
                LivingEntity.class,
                entity.getBoundingBox().expand(4),
                it ->
                    it.getHealth() < entityHealth &&
                    it.hasStatusEffect(StatusEffects.WITHER)
            );

            LivingEntity transferTarget = null;
            for (LivingEntity otherEntity : weakerEntities) {
                float otherEnergy = this.getEntityEnergy(otherEntity);
                float missingEnergy = otherEntity.getMaxHealth() - otherEnergy;

                if (missingEnergy <= 0)
                    continue;

                if (entitySoulQuantity == 0 && entityHealth != 1) {
                    entity.world.spawnEntity(new SoulEntity(entity));
                    return; // Prevents applyUpdateEffect from being cancelled
                }

                if (entitySoulQuantity <= 0)
                    continue;

                if (transferTarget != null) {
                    float transferTargetEnergy = this.getEntityEnergy(transferTarget);
                    boolean isTransferValid =
                        otherEnergy < transferTargetEnergy ||
                        otherEnergy == transferTargetEnergy &&
                        otherEntity.distanceTo(entity) < transferTarget.distanceTo(entity);
                    if (isTransferValid)
                        transferTarget = otherEntity;
                } else
                    transferTarget = otherEntity;
            }

            if (transferTarget != null)
                this.transferEntitySoulTo(entity, transferTarget);

            ci.cancel();
        }
    }

    private float getEntityEnergy(LivingEntity entity) {
        return entity.getHealth() + ((WitheryLivingEntity) entity).getSoulQuantity();
    }

    private void transferEntitySoulTo(LivingEntity source, LivingEntity target) {
        ((WitheryLivingEntity) source).transferSoulTo((WitheryLivingEntity) target);
    }
}
