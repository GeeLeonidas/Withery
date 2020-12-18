package io.github.geeleonidas.withery.util;

import io.github.geeleonidas.withery.entity.SoulEntity;

public interface WitheryLivingEntity {
    void claimSoul(SoulEntity soulEntity);
    void unclaimSoul(SoulEntity soulEntity);
}
