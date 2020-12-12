package io.github.geeleonidas.withery.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface WitheryServerWorld {
    @Nullable Entity getEntityByUuid(UUID uuid);
    @Nullable LivingEntity getLivingEntityByUuid(UUID uuid);
}
