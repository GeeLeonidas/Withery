package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.Withery;
import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements WitheryLivingEntity {
    @Shadow public abstract boolean isDead();

    private final HashSet<SoulEntity> ownedSouls = new HashSet<>();
    
    protected void forEachSoul(Consumer<SoulEntity> consumer) {
        ownedSouls.forEach(consumer);
    }

    protected void removeAllSouls() {
        this.forEachSoul(Entity::remove);
        ownedSouls.clear();
    }

    protected void unclaimAllSouls() {
        this.forEachSoul(soulEntity -> soulEntity.setBoundEntity(null));
        ownedSouls.clear();
    }

    protected int getSoulQuantity() {
        return ownedSouls.size();
    }

    @Override
    public void claimSoul(SoulEntity soulEntity) {
        assert soulEntity.getBoundEntityId() == this.getEntityId();
        ownedSouls.add(soulEntity);
    }

    @Override
    public void unclaimSoul(SoulEntity soulEntity) {
        if(!ownedSouls.remove(soulEntity))
            throw new NoSuchElementException();
        soulEntity.setBoundEntity(null);
        Withery.INSTANCE.log(ownedSouls.size(), Level.INFO);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        this.unclaimAllSouls();
    }

    @Override
    public void moveToWorld(ServerWorld destination, CallbackInfoReturnable<@Nullable Entity> cir) {
        Entity destEntity = cir.getReturnValue();
        if (destEntity != null)
            this.moveOwnedSoulsToWorld(destination, (LivingEntity) destEntity);
    }

    protected void moveOwnedSoulsToWorld(ServerWorld destination, LivingEntity destEntity) {
        int soulQuantity = this.getSoulQuantity();
        this.removeAllSouls();
        for (int i = 0; i < soulQuantity; i++)
            destination.spawnEntity(new SoulEntity(destEntity));
    }
}
