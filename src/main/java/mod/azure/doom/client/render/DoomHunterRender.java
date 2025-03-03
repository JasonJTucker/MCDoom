package mod.azure.doom.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

import mod.azure.doom.client.models.DoomHunterModel;
import mod.azure.doom.entity.tiersuperheavy.DoomHunterEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DoomHunterRender extends GeoEntityRenderer<DoomHunterEntity> {

	public DoomHunterRender(EntityRendererFactory.Context renderManagerIn) {
		super(renderManagerIn, new DoomHunterModel());
	}

	@Override
	public RenderLayer getRenderType(DoomHunterEntity animatable, float partialTicks, MatrixStack stack,
			VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
			Identifier textureLocation) {
		return RenderLayer.getEntityCutout(this.getTextureResource(animatable));
	}

	@Override
	protected float getDeathMaxRotation(DoomHunterEntity entityLivingBaseIn) {
		return 0.0F;
	}

	@Override
	public void render(GeoModel model, DoomHunterEntity animatable, float partialTicks, RenderLayer type,
			MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder,
			int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder,
				packedLightIn, packedOverlayIn, red, green, blue, alpha);
		if (animatable.getDataTracker().get(DoomHunterEntity.DEATH_STATE) == 0) {
			model.getBone("sled").get().setHidden(false);
		}
		if (animatable.getDataTracker().get(DoomHunterEntity.DEATH_STATE) == 1) {
			model.getBone("sled").get().setHidden(true);
		}
	}

}