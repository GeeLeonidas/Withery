package io.github.geeleonidas.withery

import io.github.geeleonidas.withery.entity.SoulEntity
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import kotlin.reflect.jvm.javaConstructor

object Withery {
    const val modId = "withery"
    const val modName = "Withery"

    val modItemGroup: ItemGroup = ItemGroup.MISC
    private val logger = LogManager.getLogger()

    val soulEntityType: EntityType<SoulEntity> = Registry.register(
            Registry.ENTITY_TYPE, makeId("soul"),
            FabricEntityTypeBuilder.create<SoulEntity>(SpawnGroup.MISC) { type, world -> SoulEntity(type, world) }
                    .dimensions(EntityDimensions.fixed(0.125f, 0.125f)).build()
    )

    fun log(msg: String, level: Level = Level.INFO) =
        logger.log(level, "[$modName] $msg")
    fun log(obj: Any, level: Level = Level.INFO) =
        logger.log(level, "[$modName] $obj")
    fun makeId(id: String) = Identifier(modId, id)
}

fun init() {
    Withery.log("Minecraft withered away!")
}