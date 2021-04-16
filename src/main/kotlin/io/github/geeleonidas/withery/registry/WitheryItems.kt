package io.github.geeleonidas.withery.registry

import io.github.geeleonidas.withery.item.WitherBoneItem
import io.github.geeleonidas.withery.util.Loadable
import net.minecraft.item.Item

object WitheryItems: Loadable {
    val allItems = mutableListOf<Item>()
    val witherBone = WitherBoneItem().register()
}