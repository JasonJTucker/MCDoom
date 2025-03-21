package mod.azure.doom.client.models;

import mod.azure.doom.DoomMod;
import mod.azure.doom.entity.tiersuperheavy.DoomHunterEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class DoomHunterModel extends AnimatedTickingGeoModel<DoomHunterEntity> {

	private static final Identifier[] TEX = { new Identifier(DoomMod.MODID, "textures/entity/doomhunter.png"),
			new Identifier(DoomMod.MODID, "textures/entity/doomhunter_1.png"),
			new Identifier(DoomMod.MODID, "textures/entity/doomhunter_2.png"),
			new Identifier(DoomMod.MODID, "textures/entity/doomhunter_3.png"),
			new Identifier(DoomMod.MODID, "textures/entity/doomhunter_4.png"),
			new Identifier(DoomMod.MODID, "textures/entity/doomhunter_5.png"),
			new Identifier(DoomMod.MODID, "textures/entity/doomhunter_6.png"),
			new Identifier(DoomMod.MODID, "textures/entity/doomhunter_7.png") };

	@Override
	public Identifier getModelResource(DoomHunterEntity object) {
		return new Identifier(DoomMod.MODID, "geo/doomhunter.geo.json");
	}

	@Override
	public Identifier getTextureResource(DoomHunterEntity object) {
		return TEX[(object.getFlameTimer())];
	}

	@Override
	public Identifier getAnimationResource(DoomHunterEntity object) {
		return new Identifier(DoomMod.MODID, "animations/doomhunter.animation.json");
	}

	@Override
	public void setLivingAnimations(DoomHunterEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
		super.setLivingAnimations(entity, uniqueID, customPredicate);
		IBone head = this.getAnimationProcessor().getBone("neck");

		EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
		if (head != null) {
			head.setRotationX(
					Vec3f.POSITIVE_X.getRadialQuaternion(extraData.headPitch * ((float) Math.PI / 270F)).getX());
			head.setRotationY(
					Vec3f.POSITIVE_Y.getRadialQuaternion(extraData.netHeadYaw * ((float) Math.PI / 270F)).getY());
		}
	}

}