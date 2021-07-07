package io.github.geeleonidas.withery.mixin;

import io.github.geeleonidas.withery.entity.SoulEntity;
import io.github.geeleonidas.withery.network.SoulSpawnS2CPacket;
import io.github.geeleonidas.withery.util.WitheryClientPlayPacketListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements WitheryClientPlayPacketListener {
    @Shadow private MinecraftClient client;

    @Shadow private ClientWorld world;

    @Override
    public void onSoulSpawn(SoulSpawnS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);

        double x = packet.getX();
        double y = packet.getY();
        double z = packet.getZ();
        Vec3d offsetPos = new Vec3d(
            packet.getOffsetX(),
            packet.getOffsetY(),
            packet.getOffsetZ()
        );
        double accFactor = packet.getAccFactor();

        SoulEntity soulEntity;
        Entity bound = this.world.getEntityById(packet.getBoundId());
        if (bound instanceof LivingEntity)
            soulEntity = new SoulEntity((LivingEntity) bound, offsetPos, accFactor);
        else
            soulEntity = new SoulEntity(this.world, x, y, z, offsetPos, accFactor);

        soulEntity.updateTrackedPosition(x, y, z);
        soulEntity.setEntityId(packet.getEntityId());
        this.world.addEntity(packet.getEntityId(), soulEntity);
    }
}
