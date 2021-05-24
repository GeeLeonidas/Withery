package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.util.WitheryLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements WitheryLivingEntity {
    @Shadow public abstract float getHealth();

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow public abstract float getMaxHealth();

    private final ArrayList<SoulEntity> boundSouls = new ArrayList<>();
    private int tagSoulQuantity = 0;
    private int soulTime = 0;

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

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo ci) {
        if (this.boundSouls.isEmpty())
            return;

        float overflow = this.getPotentialHealth() - this.getMaxHealth();
        for (int i = 0; i < overflow; i++)
            this.unboundSoul(this.getLastSoul());

        if (this.soulTime > 0) {
            this.soulTime--;
            return;
        }

        this.soulTime = 20; // maxSoulTime

        if (this.hasStatusEffect(StatusEffects.WITHER)) // Checks every 20 ticks
            this.tickSoulTransfer();
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource source, CallbackInfo ci) {
        this.unboundAllSouls();
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("soul_quantity"))
            this.tagSoulQuantity = tag.getInt("soul_quantity");
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        if (this.tagSoulQuantity > 0)
            tag.putInt("soul_quantity", this.tagSoulQuantity);
    }

    // Interface Overrides

    @Override
    public void boundSoul(SoulEntity soulEntity) {
        this.boundSouls.add(soulEntity);
        soulEntity.setBoundEntity(this.getInstance());
        this.tagSoulQuantity++;
    }

    @Override
    public void unboundSoul(SoulEntity soulEntity) {
        this.boundSouls.remove(soulEntity);
        soulEntity.setBoundEntity(null);
        this.tagSoulQuantity--;
    }

    @Override
    public boolean containsSoul(SoulEntity soulEntity) {
        return this.boundSouls.contains(soulEntity);
    }

    @Override
    public float getPotentialHealth() {
        return this.getHealth() + this.boundSouls.size();
    }

    @Override
    public void onLoad(ServerWorld world) {
        this.loadSouls(world);
    }

    // Util functions

    @SuppressWarnings("all") // Evil wizardry 😈
    private LivingEntity getInstance() {
        return (LivingEntity) ((Object) this);
    }

    private SoulEntity getLastSoul() {
        return this.boundSouls.get(this.boundSouls.size() - 1);
    }

    protected void removeAllSouls() {
        for (SoulEntity soulEntity : this.boundSouls)
            soulEntity.remove();
        this.boundSouls.clear();
    }

    protected void unboundAllSouls() {
        while (!this.boundSouls.isEmpty())
            this.unboundSoul(this.getLastSoul());
        this.soulTime = 0;
    }

    protected void tickSoulTransfer() {
        Box aoe = this.getBoundingBox().expand(4);
        List<LivingEntity> allLiving = this.world.getEntitiesByClass(LivingEntity.class, aoe, null);
        allLiving.remove(this.getInstance());

        LivingEntity transferTarget = null;
        double lowestDist = -1;
        for (LivingEntity otherEntity : allLiving) {
            double currentDist = otherEntity.getPos().squaredDistanceTo(this.getPos());
            float otherPotHealth = ((WitheryLivingEntity) otherEntity).getPotentialHealth();
            if (
                otherEntity.hasStatusEffect(StatusEffects.WITHER) &&
                otherPotHealth < otherEntity.getMaxHealth() &&
                otherPotHealth < this.getPotentialHealth() &&
                (transferTarget == null || currentDist < lowestDist)
            ) {
                transferTarget = otherEntity;
                lowestDist = currentDist;
            }
        }

        if (transferTarget != null)
            ((WitheryLivingEntity) transferTarget).boundSoul(this.getLastSoul());
    }

    protected void loadSouls(ServerWorld world) {
        final int soulQuantity = this.tagSoulQuantity;
        this.tagSoulQuantity = 0;

        for (int i = 0; i < soulQuantity; i++)
            world.loadEntity(new SoulEntity(this.getInstance()));
    }
}
