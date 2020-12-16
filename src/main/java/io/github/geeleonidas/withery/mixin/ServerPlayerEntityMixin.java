package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
    private int tagSoulQuantity = 0;
    private boolean hasDiedRecently = false;

    @SuppressWarnings("all") // Evil wizardry 😈
    private ServerPlayerEntity getInstance() {
        return (ServerPlayerEntity) ((Object) this);
    }

    @Inject(at = @At("RETURN"), method = "moveToWorld")
    public void movePlayerToWorld(ServerWorld destination, CallbackInfoReturnable<@Nullable Entity> cir) {
        this.moveOwnedSoulsToWorld(destination, (LivingEntity) cir.getReturnValue());
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        this.unclaimAllSouls();
        hasDiedRecently = true;
    }

    @Inject(at = @At("TAIL"), method = "onSpawn")
    public void onPlayerSpawn(CallbackInfo ci) {
        if (this.isDead()) // Occurs when a dead player joins
            hasDiedRecently = true;
        else if (!hasDiedRecently) // Occurs when an alive player joins
            for (int i = 0; i < tagSoulQuantity; i++)
                this.world.spawnEntity(new SoulEntity(this.getInstance()));
        else // Occurs when a player respawns
            hasDiedRecently = false;
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        tagSoulQuantity = tag.getInt("soul_quantity");
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("soul_quantity", this.getSoulQuantity());
    }
}
