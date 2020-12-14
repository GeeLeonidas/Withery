package io.github.geeleonidas.withery.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntityMixin {
    @Inject(at = @At("RETURN"), method = "moveToWorld")
    public void movePlayerToWorld(ServerWorld destination, CallbackInfoReturnable<@Nullable Entity> cir) {
        this.moveOwnedSoulsToWorld(destination, (LivingEntity) cir.getReturnValue());
    }
}
