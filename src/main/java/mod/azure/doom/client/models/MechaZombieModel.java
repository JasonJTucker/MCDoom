package mod.azure.doom.client.models;

import mod.azure.doom.DoomMod;
import mod.azure.doom.entity.tierfodder.MechaZombieEntity;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class MechaZombieModel extends AnimatedTickingGeoModel<MechaZombieEntity> {

	@Override
	public Identifier getModelResource(MechaZombieEntity object) {
		return new Identifier(DoomMod.MODID, "geo/mechazombie.geo.json");
	}

	@Override
	public Identifier getTextureResource(MechaZombieEntity object) {
		return new Identifier(DoomMod.MODID, "textures/entity/mechazombie.png");
	}

	@Override
	public Identifier getAnimationResource(MechaZombieEntity object) {
		return new Identifier(DoomMod.MODID, "animations/mechazombie_animation.json");
	}

	@Override
	public void setLivingAnimations(MechaZombieEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
		super.setLivingAnimations(entity, uniqueID, customPredicate);
		IBone head = this.getAnimationProcessor().getBone("head");

		EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
		if (head != null) {
			head.setRotationX(
					Vec3f.POSITIVE_X.getRadialQuaternion(extraData.headPitch * ((float) Math.PI / 360F)).getX());
			head.setRotationY(
					Vec3f.POSITIVE_Y.getRadialQuaternion(extraData.netHeadYaw * ((float) Math.PI / 340F)).getY());
		}
	}
}