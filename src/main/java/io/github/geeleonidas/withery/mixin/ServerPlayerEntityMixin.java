package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.util.WitheryServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin implements WitheryServerPlayerEntity {
    @Inject(at = @At("RETURN"), method = "moveToWorld")
    public void movePlayerToWorld(ServerWorld destination, CallbackInfoReturnable<@Nullable Entity> cir) {
        this.loadSouls(destination);
    }

    @Override
    public void onConnect(ServerWorld world) {
        this.loadSouls(world);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        this.unboundAllSouls();
    }
}
