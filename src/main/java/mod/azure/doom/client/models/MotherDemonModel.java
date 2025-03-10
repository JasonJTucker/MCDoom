package mod.azure.doom.client.models;

import mod.azure.doom.DoomMod;
import mod.azure.doom.entity.tierboss.MotherDemonEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class MotherDemonModel extends AnimatedTickingGeoModel<MotherDemonEntity> {

	@Override
	public Identifier getModelResource(MotherDemonEntity object) {
		return new Identifier(DoomMod.MODID, "geo/motherdemon.geo.json");
	}

	@Override
	public Identifier getTextureResource(MotherDemonEntity object) {
		return new Identifier(DoomMod.MODID, "textures/entity/motherdemon.png");
	}

	@Override
	public Identifier getAnimationResource(MotherDemonEntity object) {
		return new Identifier(DoomMod.MODID, "animations/motherdemon.animation.json");
	}
}