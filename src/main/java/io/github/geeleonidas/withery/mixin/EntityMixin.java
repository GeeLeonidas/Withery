package io.github.geeleonidas.withery.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;

    @Shadow public abstract int getEntityId();

    @Inject(at = @At(value = "RETURN", ordinal = 2), method = "moveToWorld")
    public void moveToWorld(ServerWorld destination, CallbackInfoReturnable<@Nullable Entity> cir) { }
}
