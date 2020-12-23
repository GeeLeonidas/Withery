package io.github.geeleonidas.withery.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;

    @Inject(at = @At("HEAD"), method = "remove")
    public void remove(CallbackInfo ci) { }

    @Inject(at = @At("RETURN"), method = "moveToWorld")
    public void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) { }
}
