package io.github.geeleonidas.withery.util;

import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientPlayPacketListener;

public interface WitheryClientPlayPacketListener extends ClientPlayPacketListener {
    void onSoulSpawn(SoulSpawnS2CPacket packet);
}
