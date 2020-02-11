package tschipp.buildersbag.common.caps;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.RegistryHandler;
import tschipp.buildersbag.common.inventory.BagItemStackHandler;

public class BagCapStorage implements IStorage<IBagCap>
{

	@Override
	public NBTBase writeNBT(Capability<IBagCap> capability, IBagCap instance, EnumFacing side)
	{
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound inventory = instance.getBlockInventory().serializeNBT();
		NBTTagList modules = new NBTTagList();
		
		for(IBagModule module : instance.getModules())
		{
			modules.appendTag(module.serializeNBT());
		}
		
		tag.setTag("inventory", inventory);
		tag.setTag("modules", modules);
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IBagCap> capability, IBagCap instance, EnumFacing side, NBTBase nbt)
	{
		NBTTagCompound tag = (NBTTagCompound) nbt;
		NBTTagCompound inventory = tag.getCompoundTag("inventory");
		NBTTagList modules = tag.getTagList("modules", 10);

		BagItemStackHandler handler = new BagItemStackHandler(0,0);
		handler.deserializeNBT(inventory);
		instance.setBlockInventory(handler);
		
		List<IBagModule> parsedModules = new ArrayList<IBagModule>();
		for(int i = 0; i < modules.tagCount(); i++)
		{
			NBTTagCompound module = modules.getCompoundTagAt(i);
			IBagModule mod = RegistryHandler.getModule(new ResourceLocation(module.getString("name")));
			if(mod != null)
			{
				mod.deserializeNBT(module);
				parsedModules.add(mod);
			}
		}
		
		instance.setModules(parsedModules.toArray(new IBagModule[parsedModules.size()]));
	}

}