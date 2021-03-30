package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.util.WitheryServerPlayerEntity;
import io.github.geeleonidas.withery.util.WitheryServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess, WitheryServerWorld {
    @Shadow @Nullable protected abstract Entity checkIfUuidExists(UUID uUID);

    @Shadow public abstract ServerWorld toServerWorld();

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Override
    public @Nullable Entity getEntityByUuid(UUID uuid) {
        return this.checkIfUuidExists(uuid);
    }

    @Override
    public @Nullable LivingEntity getLivingEntityByUuid(UUID uuid) {
        return (LivingEntity) getEntityByUuid(uuid);
    }

    @Inject(at = @At("TAIL"), method = "onPlayerConnected")
    private void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        ((WitheryServerPlayerEntity) player).onConnect(this.toServerWorld());
    }
}
