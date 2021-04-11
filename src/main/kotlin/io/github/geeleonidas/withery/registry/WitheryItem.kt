package io.github.geeleonidas.withery.registry

import io.github.geeleonidas.withery.Withery
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry

interface WitheryItem {
    val id: String

    fun register(): Item {
        Registry.register(Registry.ITEM, Withery.makeId(this.id), this as Item)
        WitheryItems.allItems.add(this)
        return this
    }
}