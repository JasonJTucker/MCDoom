package mod.azure.doom.client.models.projectiles;

import mod.azure.doom.DoomMod;
import mod.azure.doom.entity.projectiles.entity.DoomFireEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ArchvileFiringModel extends AnimatedGeoModel<DoomFireEntity> {
	@Override
	public Identifier getModelResource(DoomFireEntity object) {
		return new Identifier(DoomMod.MODID, "geo/archvilefiring.geo.json");
	}

	@Override
	public Identifier getTextureResource(DoomFireEntity object) {
		return new Identifier(DoomMod.MODID, "textures/items/empty.png");
	}

	@Override
	public Identifier getAnimationResource(DoomFireEntity animatable) {
		return new Identifier(DoomMod.MODID, "animations/archvilefiring.animation.json");
	}
}
