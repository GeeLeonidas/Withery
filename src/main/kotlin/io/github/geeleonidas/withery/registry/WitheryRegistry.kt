package io.github.geeleonidas.withery.registry

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.entity.SoulEntity
import io.github.geeleonidas.withery.item.WitherBoneItem
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry

object WitheryItems: Loadable {
    val allItems = mutableListOf<Item>()
    val witherBone = WitherBoneItem().register()
}

object WitheryEntityTypes: Loadable {
    val soulEntity: EntityType<SoulEntity> = Registry.register(
        Registry.ENTITY_TYPE, Withery.makeId("soul"),
        FabricEntityTypeBuilder.create<SoulEntity>(SpawnGroup.MISC) { type, world -> SoulEntity(type, world) }
            .dimensions(EntityDimensions.fixed(SoulEntity.sideLength, SoulEntity.sideLength)).build()
    )
}

object WitheryRegistry: Loadable {
    init {
        WitheryItems.load()
        WitheryEntityTypes.load()
    }
}

interface Loadable {
    fun load() { }
}