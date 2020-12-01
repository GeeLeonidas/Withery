package io.github.geeleonidas.withery.client

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.client.render.entity.SoulEntityRenderer
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EntityRenderDispatcher

fun init() {
    EntityRendererRegistry.INSTANCE.register(Withery.soulEntityType)
    { dispatcher: EntityRenderDispatcher, _: EntityRendererRegistry.Context -> SoulEntityRenderer(dispatcher) }
}