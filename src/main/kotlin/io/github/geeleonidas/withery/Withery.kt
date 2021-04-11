package io.github.geeleonidas.withery

import io.github.geeleonidas.withery.event.WitheryEntityLoadEvent
import io.github.geeleonidas.withery.registry.WitheryItems
import io.github.geeleonidas.withery.registry.WitheryRegistry
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

object Withery {
    const val modId = "withery"
    const val modName = "Withery"

    private val logger = LogManager.getLogger()

    val mainItemGroup = FabricItemGroupBuilder.create(makeId("main")).appendItems {
        for (item in WitheryItems.allItems)
            it.add(ItemStack(item))
    }.build()

    fun log(msg: String, level: Level = Level.INFO) =
        logger.log(level, "[$modName] $msg")
    fun log(obj: Any, level: Level = Level.INFO) =
        logger.log(level, "[$modName] $obj")
    fun makeId(id: String) = Identifier(modId, id)
}

fun init() {
    WitheryRegistry.load()
    ServerEntityEvents.ENTITY_LOAD.register(WitheryEntityLoadEvent)
    Withery.log("Minecraft withered away!")
}