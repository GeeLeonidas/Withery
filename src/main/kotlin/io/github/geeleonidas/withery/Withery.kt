package io.github.geeleonidas.withery

import io.github.geeleonidas.withery.entity.SoulEntity
import io.github.geeleonidas.withery.event.WitheryEntityLoadEvent
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

object Withery {
    const val modId = "withery"
    const val modName = "Withery"

    val modItemGroup: ItemGroup = ItemGroup.MISC
    private val logger = LogManager.getLogger()

    val soulEntityType: EntityType<SoulEntity> = Registry.register(
            Registry.ENTITY_TYPE, makeId("soul"),
            FabricEntityTypeBuilder.create<SoulEntity>(SpawnGroup.MISC) { type, world -> SoulEntity(type, world) }
                    .dimensions(EntityDimensions.fixed(SoulEntity.sideLength, SoulEntity.sideLength)).build()
    )

    fun log(msg: String, level: Level = Level.INFO) =
        logger.log(level, "[$modName] $msg")
    fun log(obj: Any, level: Level = Level.INFO) =
        logger.log(level, "[$modName] $obj")
    fun makeId(id: String) = Identifier(modId, id)
}

fun init() {
    ServerEntityEvents.ENTITY_LOAD.register(WitheryEntityLoadEvent)
    Withery.log("Minecraft withered away!")
}