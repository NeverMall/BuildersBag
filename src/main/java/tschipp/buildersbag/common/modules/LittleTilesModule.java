package tschipp.buildersbag.common.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.common.caps.IBagCap;

public class LittleTilesModule extends AbstractBagModule
{
	private static final ItemStack DISPLAY = new ItemStack(Item.getByNameOrId("littletiles:chisel"));

	public LittleTilesModule()
	{
		super("buildersbag:littletiles");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag)
	{
		return NonNullList.create();
	}

	@Override
	public ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return true;
	}

	@Override
	public String[] getModDependencies()
	{
		return new String[] {"littletiles"};
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return null;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}
	
	

}
