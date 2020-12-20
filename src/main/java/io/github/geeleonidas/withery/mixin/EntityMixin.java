package io.github.geeleonidas.withery.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;

    @Inject(at = @At("HEAD"), method = "remove")
    public void remove(CallbackInfo ci) { }
}
