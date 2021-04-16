package io.github.geeleonidas.withery.item

import io.github.geeleonidas.withery.registry.entry.WitheryItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item

class WitherBoneItem: Item(FabricItemSettings()), WitheryItem {
    override val id = "wither_bone"
}