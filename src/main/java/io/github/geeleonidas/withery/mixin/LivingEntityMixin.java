package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements WitheryLivingEntity {
    public LivingEntityMixin(EntityType<?> type, World world) { super(type, world); }

    private final HashSet<Integer> ownedSoulsIds = new HashSet<>();

    private void forEachOwnedSoul(Consumer<SoulEntity> consumer) {
        for (Integer entityId : ownedSoulsIds)
            consumer.accept((SoulEntity) this.world.getEntityById(entityId));
    }

    @Override
    public void claimSoul(SoulEntity soulEntity) {
        assert soulEntity.getBoundEntityId() == this.getEntityId();
        ownedSoulsIds.add(soulEntity.getEntityId());
    }

    @Override
    public void unclaimSoul(SoulEntity soulEntity) {
        if (ownedSoulsIds.remove(soulEntity.getEntityId()))
            soulEntity.setBoundEntity(null);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        forEachOwnedSoul(soulEntity -> soulEntity.setBoundEntity(null));
        ownedSoulsIds.clear();
    }

    // TODO: Figure out why this isn't working
    @Override
    public @Nullable Entity moveToWorld(ServerWorld destination) {
        Entity result = super.moveToWorld(destination);
        if (result != null)
            forEachOwnedSoul(soulEntity -> soulEntity.moveToWorld(destination));
        return result;
    }
}
