package io.github.geeleonidas.withery.client.render.entity

import io.github.geeleonidas.withery.Withery
import io.github.geeleonidas.withery.entity.SoulEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
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
    private val textureIds: List<Identifier>
    private val textureLayers: List<RenderLayer>

    init {
        this.shadowRadius = 0F
        this.shadowOpacity = 0F

        val tempIds = mutableListOf<Identifier>()
        for (i in 1..8) tempIds += Withery.makeId("textures/entity/soul/soul$i.png")
        val tempLayers = mutableListOf<RenderLayer>()
        tempIds.forEach { tempLayers += RenderLayer.getEntityTranslucent(it) }
        textureIds = tempIds.toList()
        textureLayers = tempLayers.toList()
    }

    override fun shouldRender(entity: SoulEntity, frustum: Frustum, x: Double, y: Double, z: Double): Boolean {
        val isThirdPerson = MinecraftClient.getInstance().gameRenderer.camera.isThirdPerson

        return if (isThirdPerson)
            super.shouldRender(entity, frustum, x, y, z)
        else
            false
    }

    override fun render(entity: SoulEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        matrices.push()

        // UV Settings
        val ux = 0f
        val uy = 1f
        val vx = 0f
        val vy = 1f

        // Basic config (entity will be facing the player)
        matrices.scale(0.5f, 0.5f, 0.5f)
        matrices.translate(0.0, -0.125, 0.0)
        matrices.multiply(this.renderManager.rotation)
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180F))

        // Who doesn't like details?
        val time = entity.age + tickDelta
        val velLenSq = entity.velocity.lengthSquared()
        matrices.translate(0.0, sin(time * PI / 20) / (4 + 4 * velLenSq), 0.0)

        // Don't ask me, I don't know either
        val vertexConsumer = vertexConsumers.getBuffer(textureLayers[getCurrentFrame(entity.age)])
        val entry = matrices.peek()
        val matrix4f = entry.model
        val matrix3f = entry.normal
        draw(vertexConsumer, matrix4f, matrix3f, -0.5f, -0.25f, ux, vy)
        draw(vertexConsumer, matrix4f, matrix3f, 0.5f, -0.25f, uy, vy)
        draw(vertexConsumer, matrix4f, matrix3f, 0.5f, 0.75f, uy, vx)
        draw(vertexConsumer, matrix4f, matrix3f, -0.5f, 0.75f, ux, vx)

        matrices.pop()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    private fun draw(vertexConsumer: VertexConsumer, matrix4f: Matrix4f, matrix3f: Matrix3f, x: Float, y: Float, u: Float, v: Float, alpha: Int = 255) {
        vertexConsumer.vertex(matrix4f, x, y, 0.0f).color(255, 255, 255, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrix3f, 0.0f, 1.0f, 0.0f).next()
    }

    private fun getCurrentFrame(currentTick: Int) = (currentTick / 2) % textureIds.size

    override fun getTexture(entity: SoulEntity): Identifier = textureIds[getCurrentFrame(entity.age)]
}