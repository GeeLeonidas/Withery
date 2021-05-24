package io.github.geeleonidas.withery.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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

    @Shadow public abstract Box getBoundingBox();

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract Iterable<ItemStack> getItemsHand();

    @Shadow public double prevX;

    @Inject(at = @At("HEAD"), method = "remove")
    protected void remove(CallbackInfo ci) { }

    @Inject(at = @At("RETURN"), method = "moveToWorld")
    protected void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) { }
}
