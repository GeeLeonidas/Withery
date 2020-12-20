package io.github.geeleonidas.withery.event

import io.github.geeleonidas.withery.util.WitheryLivingEntity
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

object WitheryEntityLoadEvent: ServerEntityEvents.Load {
    override fun onLoad(entity: Entity, world: ServerWorld) {
        if (entity is WitheryLivingEntity && entity !is ServerPlayerEntity)
            entity.onLoad(world)
    }
}