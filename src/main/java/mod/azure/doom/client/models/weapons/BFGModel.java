package mod.azure.doom.client.models.weapons;

import mod.azure.doom.DoomMod;
import mod.azure.doom.item.weapons.BFG;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BFGModel extends AnimatedGeoModel<BFG> {
	@Override
	public Identifier getModelResource(BFG object) {
		return new Identifier(DoomMod.MODID, "geo/bfgeternal.geo.json");
	}

	@Override
	public Identifier getTextureResource(BFG object) {
		return new Identifier(DoomMod.MODID, "textures/items/bfgeternal.png");
	}

	@Override
	public Identifier getAnimationResource(BFG animatable) {
		return new Identifier(DoomMod.MODID, "animations/bfg.animation.json");
	}
}
