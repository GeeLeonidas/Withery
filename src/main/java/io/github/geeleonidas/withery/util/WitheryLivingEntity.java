package io.github.geeleonidas.withery.util;

import io.github.geeleonidas.withery.entity.SoulEntity;
import net.minecraft.server.world.ServerWorld;

public interface WitheryLivingEntity {
    void claimSoul(SoulEntity soulEntity);
    void unclaimSoul(SoulEntity soulEntity);
    void onLoad(ServerWorld world);
}
