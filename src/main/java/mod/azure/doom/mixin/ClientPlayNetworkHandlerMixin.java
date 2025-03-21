package mod.azure.doom.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.doom.entity.projectiles.MeatHookEntity;
import mod.azure.doom.util.registry.ProjectilesEntityRegister;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	private ClientWorld world;

	@Inject(method = "onEntitySpawn", at = @At("TAIL"))
	private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo callbackInfo) {
		EntityType<?> type = packet.getEntityTypeId();
		double x = packet.getX();
		double y = packet.getY();
		double z = packet.getZ();
		PersistentProjectileEntity entity = null;

		if (type == ProjectilesEntityRegister.MEATHOOOK_ENTITY)
			entity = new MeatHookEntity(world, x, y, z);

		if (entity != null) {
			Entity owner = world.getEntityById(packet.getEntityData());

			if (owner != null)
				entity.setOwner(owner);

			int id = packet.getId();
			entity.updatePosition(x, y, z);
			entity.syncPacketPositionCodec(x, y, z);
			entity.setPitch((float) (packet.method_11171() * 360) / 256f);
			entity.setYaw((float) (packet.method_11168() * 360) / 256f);
			entity.setId(id);
			entity.setUuid(packet.getUuid());
			world.addEntity(id, entity);
		}
	}
}