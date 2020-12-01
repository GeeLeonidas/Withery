package io.github.geeleonidas.withery.client.render.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.entity.SoulEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.util.Identifier
import net.minecraft.util.math.Matrix3f
import net.minecraft.util.math.Matrix4f
import kotlin.math.PI
import kotlin.math.sin


@Environment(EnvType.CLIENT)
class SoulEntityRenderer(entityRenderDispatcher: EntityRenderDispatcher):
        EntityRenderer<SoulEntity>(entityRenderDispatcher) {
    private val textureId = Withery.makeId("textures/entity/soul/soul.png")
    private val textureLayer: RenderLayer = RenderLayer.getEntityTranslucent(textureId)

    init {
        this.shadowRadius = 0F
        this.shadowOpacity = 0F
    }

    override fun render(entity: SoulEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    private fun draw(vertexConsumer: VertexConsumer, matrix4f: Matrix4f, matrix3f: Matrix3f, x: Float, y: Float, alpha: Int, u: Float, v: Float) {
        vertexConsumer.vertex(matrix4f, x, y, 0.0f).color(255, 255, 255, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrix3f, 0.0f, 1.0f, 0.0f).next()
    }

    override fun getTexture(entity: SoulEntity): Identifier = textureId
}