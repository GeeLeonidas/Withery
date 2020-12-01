package io.github.geeleonidas.withery

import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

object Withery {
    const val modId = "withery"
    const val modName = "Withery"

    val modItemGroup: ItemGroup = ItemGroup.MISC
    private val logger = LogManager.getLogger()

    fun log(msg: Any, level: Level = Level.INFO) =
        logger.log(level, "[$modName] $msg")
    fun makeId(id: String) = Identifier(modId, id)
}

@Suppress("unused")
fun init() {
    Withery.log("Skibidabedidou")
}