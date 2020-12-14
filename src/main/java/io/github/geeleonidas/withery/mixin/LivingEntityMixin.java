package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements WitheryLivingEntity {
    protected final HashSet<SoulEntity> ownedSouls = new HashSet<>();

    @Override
    public void claimSoul(SoulEntity soulEntity) {
        assert soulEntity.getBoundEntityId() == this.getEntityId();
        ownedSouls.add(soulEntity);
    }

    @Override
    public void unclaimSoul(SoulEntity soulEntity) {
        if (ownedSouls.remove(soulEntity))
            soulEntity.setBoundEntity(null);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        ownedSouls.forEach(soulEntity -> soulEntity.setBoundEntity(null));
        ownedSouls.clear();
    }

    @Override
    public void moveToWorld(ServerWorld destination, CallbackInfoReturnable<@Nullable Entity> cir) {
        Entity destEntity = cir.getReturnValue();
        if (destEntity != null)
            this.moveOwnedSoulsToWorld(destination, (LivingEntity) destEntity);
    }

    protected void moveOwnedSoulsToWorld(ServerWorld destination, LivingEntity destEntity) {
        int soulsLength = ownedSouls.size();
        ownedSouls.forEach(Entity::remove);
        ownedSouls.clear();
        for (int i = 0; i < soulsLength; i++)
            destination.spawnEntity(new SoulEntity(destEntity));
    }
}
