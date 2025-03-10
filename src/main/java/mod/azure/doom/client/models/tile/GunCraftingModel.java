package mod.azure.doom.client.models.tile;

import mod.azure.doom.DoomMod;
import mod.azure.doom.entity.tileentity.GunBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GunCraftingModel extends AnimatedGeoModel<GunBlockEntity> {
	@Override
	public Identifier getAnimationResource(GunBlockEntity entity) {
		return new Identifier(DoomMod.MODID, "animations/gun_table.animation.json");
	}

	@Override
	public Identifier getModelResource(GunBlockEntity animatable) {
		return new Identifier(DoomMod.MODID, "geo/gun_table.geo.json");
	}

	@Override
	public Identifier getTextureResource(GunBlockEntity entity) {
		return new Identifier(DoomMod.MODID, "textures/blocks/gun_table.png");
	}
}
