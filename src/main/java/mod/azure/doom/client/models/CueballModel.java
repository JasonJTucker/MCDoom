package mod.azure.doom.client.models;

import mod.azure.doom.DoomMod;
import mod.azure.doom.entity.tierambient.CueBallEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class CueballModel extends AnimatedTickingGeoModel<CueBallEntity> {

	private static final Identifier[] TEX = { new Identifier(DoomMod.MODID, "textures/entity/cueball.png"),
			new Identifier(DoomMod.MODID, "textures/entity/cueball_flame_1.png") };

	@Override
	public Identifier getModelResource(CueBallEntity object) {
		return new Identifier(DoomMod.MODID,
				"geo/" + (object.getVariant() == 2 ? "screecher" : "cueball") + ".geo.json");
	}

	@Override
	public Identifier getTextureResource(CueBallEntity object) {
		return object.getVariant() == 2 ? new Identifier(DoomMod.MODID, "textures/entity/screecher.png")
				: TEX[(object.getFlameTimer())];
	}

	@Override
	public Identifier getAnimationResource(CueBallEntity object) {
		return new Identifier(DoomMod.MODID,
				"animations/" + (object.getVariant() == 2 ? "screecher" : "rocket") + ".animation.json");
	}

}