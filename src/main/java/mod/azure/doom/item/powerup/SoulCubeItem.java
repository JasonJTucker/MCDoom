package mod.azure.doom.item.powerup;

import java.util.List;

import dev.emi.trinkets.api.TrinketItem;
import mod.azure.doom.DoomMod;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class SoulCubeItem extends TrinketItem {

	public SoulCubeItem() {
		super(new Item.Settings().group(DoomMod.DoomPowerUPItemGroup).maxCount(1).maxDamage(5));
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(new TranslatableText("Uses Remaining: " + stack.getDamage()).formatted(Formatting.ITALIC));
		super.appendTooltip(stack, world, tooltip, context);
	}
}