package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.util.WitheryServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @SuppressWarnings("all") // Evil wizardry 😈
    private ServerWorld getInstance() {
        return (ServerWorld) ((Object) this);
    }

    @Inject(at = @At("TAIL"), method = "onPlayerConnected")
    private void onPlayerConnected(ServerPlayerEntity playerEntity, CallbackInfo ci) {
        ((WitheryServerPlayerEntity) playerEntity).onConnect(this.getInstance());
    }
}
