package mod.azure.doom.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

import mod.azure.doom.client.models.ProwlerModel;
import mod.azure.doom.entity.tierheavy.ProwlerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class ProwlerRender extends GeoEntityRenderer<ProwlerEntity> {

	public ProwlerRender(EntityRendererFactory.Context renderManagerIn) {
		super(renderManagerIn, new ProwlerModel());
	}

	@Override
	public RenderLayer getRenderType(ProwlerEntity animatable, float partialTicks, MatrixStack stack,
			VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
			Identifier textureLocation) {
		return RenderLayer.getEntityTranslucent(this.getTextureResource(animatable));
	}

	@Override
	protected float getDeathMaxRotation(ProwlerEntity entityLivingBaseIn) {
		return 0.0F;
	}

}