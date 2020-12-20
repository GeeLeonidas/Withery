package io.github.geeleonidas.withery.util;

import net.minecraft.server.world.ServerWorld;

public interface WitheryServerPlayerEntity {
    void onConnect(ServerWorld world);
}
