package io.github.geeleonidas.withery.util;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface WitheryServerWorld {
    @Nullable
    Entity getEntityByUuid(UUID uuid);
}
