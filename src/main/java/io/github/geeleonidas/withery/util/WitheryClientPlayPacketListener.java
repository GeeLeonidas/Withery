package io.github.geeleonidas.withery.util;

import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket;
import net.minecraft.network.listener.ClientPlayPacketListener;

public interface WitheryClientPlayPacketListener extends ClientPlayPacketListener {
    void onSoulSpawn(SoulSpawnS2CPacket packet);
}
