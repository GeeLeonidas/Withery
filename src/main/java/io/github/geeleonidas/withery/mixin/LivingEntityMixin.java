package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
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
    @Shadow public abstract float getHealth();

    private final HashSet<SoulEntity> boundSouls = new HashSet<>();
    private int tagSoulQuantity = 0;

    @SuppressWarnings("all") // Evil wizardry 😈
    private LivingEntity getInstance() {
        return (LivingEntity) ((Object) this);
    }
    
    protected void forEachSoul(Consumer<SoulEntity> consumer) {
        boundSouls.forEach(consumer);
    }

    protected void removeAllSouls() {
        this.forEachSoul(Entity::remove);
        boundSouls.clear();
    }

    protected void unboundAllSouls() {
        this.forEachSoul(SoulEntity::unbound);
        boundSouls.clear();
        tagSoulQuantity = 0;
    }

    protected void loadSouls(ServerWorld world) {
        final int soulQuantity = tagSoulQuantity;
        tagSoulQuantity = 0;

        for (int i = 0; i < soulQuantity; i++)
            world.loadEntity(new SoulEntity(this.getInstance()));
    }

    // Interface Overrides

    @Override
    public void boundSoul(SoulEntity soulEntity) {
        assert soulEntity.getBoundEntity() == this.getInstance();
        boundSouls.add(soulEntity);
        tagSoulQuantity++;
    }

    @Override
    public void unboundSoul(SoulEntity soulEntity) {
        if(!boundSouls.remove(soulEntity))
            throw new NoSuchElementException();
        soulEntity.unbound();
        tagSoulQuantity--;
    }

    @Override
    public void onLoad(ServerWorld world) {
        this.loadSouls(world);
    }

    @Override
    public float getPotentialHealth() {
        return this.getHealth() + boundSouls.size();
    }

    // Inject Overrides

    @Override
    protected void remove(CallbackInfo ci) {
        this.removeAllSouls();
    }

    @Override
    protected void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (cir.getReturnValue() != null)
            this.removeAllSouls();
    }

    // Injects

    @Inject(at= @At("TAIL"), method = "tick")
    private void tick(CallbackInfo ci) {

    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource source, CallbackInfo ci) {
        this.unboundAllSouls();
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("soul_quantity"))
            tagSoulQuantity = tag.getInt("soul_quantity");
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        if (tagSoulQuantity > 0)
            tag.putInt("soul_quantity", tagSoulQuantity);
    }
}
