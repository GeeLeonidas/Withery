package io.github.geeleonidas.withery.util

import net.minecraft.entity.LivingEntity
import net.minecraft.network.listener.ClientPlayPacketListener

val LivingEntity.witheryImpl: WitheryLivingEntity
    get() = this as WitheryLivingEntity

val ClientPlayPacketListener.witheryImpl: WitheryClientPlayPacketListener
    get() = this as WitheryClientPlayPacketListener

interface Loadable {
    fun load() { }
}