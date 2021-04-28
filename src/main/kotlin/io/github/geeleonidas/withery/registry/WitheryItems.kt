package io.github.geeleonidas.withery.registry

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.item.WitherBoneItem
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object WitheryItems {
    val allItems = mutableListOf<Item>()
    val witherBone = WitherBoneItem().register()

    val mainItemGroup = FabricItemGroupBuilder.create(Withery.makeId("main")).icon {
        ItemStack(Items.WITHER_ROSE)
    }.appendItems {
        for (item in allItems)
            it.add(ItemStack(item))
    }.build()

    fun load() = Unit
}