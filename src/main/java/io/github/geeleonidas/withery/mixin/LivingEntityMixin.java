package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.Withery;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) { super(type, world); }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        Withery.INSTANCE.log(this.getEntityId(), Level.INFO);
    }
}
