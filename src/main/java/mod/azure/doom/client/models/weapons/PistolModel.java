package mod.azure.doom.client.models.weapons;

import mod.azure.doom.DoomMod;
import mod.azure.doom.item.weapons.PistolItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PistolModel extends AnimatedGeoModel<PistolItem> {
	@Override
	public Identifier getModelResource(PistolItem object) {
		return new Identifier(DoomMod.MODID, "geo/pistol.geo.json");
	}

	@Override
	public Identifier getTextureResource(PistolItem object) {
		return new Identifier(DoomMod.MODID, "textures/items/pistol.png");
	}

	@Override
	public Identifier getAnimationResource(PistolItem animatable) {
		return new Identifier(DoomMod.MODID, "animations/pistol.animation.json");
	}
}
