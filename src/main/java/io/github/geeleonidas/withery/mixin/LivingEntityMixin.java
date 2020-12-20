package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements WitheryLivingEntity {
    private final HashSet<SoulEntity> ownedSouls = new HashSet<>();
    private int tagSoulQuantity = 0;

    @SuppressWarnings("all") // Evil wizardry 😈
    private LivingEntity getInstance() {
        return (LivingEntity) ((Object) this);
    }
    
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
        tagSoulQuantity = 0;
    }

    protected void loadSouls(ServerWorld world) {
        final int soulQuantity = tagSoulQuantity;
        tagSoulQuantity = 0;

        for (int i = 0; i < soulQuantity; i++)
            world.spawnEntity(new SoulEntity(this.getInstance()));
    }

    @Override
    public void claimSoul(SoulEntity soulEntity) {
        assert soulEntity.getBoundEntity() == null;
        soulEntity.setBoundEntity(this.getInstance());
        ownedSouls.add(soulEntity);
        tagSoulQuantity++;
    }

    @Override
    public void unclaimSoul(SoulEntity soulEntity) {
        if(!ownedSouls.remove(soulEntity))
            throw new NoSuchElementException();
        soulEntity.setBoundEntity(null);
        tagSoulQuantity--;
    }

    @Override
    public void remove(CallbackInfo ci) {
        this.removeAllSouls();
    }

    @Override
    public void onLoad(ServerWorld world) {
        this.loadSouls(world);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        this.unclaimAllSouls();
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("soul_quantity"))
            tagSoulQuantity = tag.getInt("soul_quantity");
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        if (tagSoulQuantity > 0)
            tag.putInt("soul_quantity", tagSoulQuantity);
    }
}
