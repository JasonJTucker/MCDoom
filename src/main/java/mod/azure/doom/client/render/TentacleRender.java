package mod.azure.doom.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

import mod.azure.doom.client.models.TentacleModel;
import mod.azure.doom.entity.tierambient.TentacleEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TentacleRender extends GeoEntityRenderer<TentacleEntity> {

	public TentacleRender(EntityRendererFactory.Context renderManagerIn) {
		super(renderManagerIn, new TentacleModel());
	}

	@Override
	public RenderLayer getRenderType(TentacleEntity animatable, float partialTicks, MatrixStack stack,
			VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
			Identifier textureLocation) {
		return RenderLayer.getEntityTranslucent(this.getTextureResource(animatable));
	}

	@Override
	protected float getDeathMaxRotation(TentacleEntity entityLivingBaseIn) {
		return 0.0F;
	}

}