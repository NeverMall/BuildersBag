package tschipp.buildersbag.common.crafting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.config.BuildersBagConfig;

public class CraftingHandler
{
	private static Map<String, List<RecipeContainer>> recipes = new HashMap<String, List<RecipeContainer>>();

	private static RecipeTree recipeTree = new RecipeTree();

	public static void generateRecipes()
	{
		
		for (IRecipe recipe : ForgeRegistries.RECIPES)
		{
			ItemStack output = recipe.getRecipeOutput();
			if (!output.isEmpty())
			{
				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				String itemString = getItemString(output);
				List<RecipeContainer> genRecipes = recipes.get(itemString);

				if (genRecipes != null)
				{
					genRecipes.add(new RecipeContainer(ingredients, output, getTierIfStaged(recipe)));
				} else
				{
					genRecipes = new ArrayList<RecipeContainer>();
					genRecipes.add(new RecipeContainer(ingredients, output, getTierIfStaged(recipe)));
					recipes.put(itemString, genRecipes);
				}

			}

			recipeTree.add(recipe);
		}
	}

	public static List<RecipeContainer> getRecipes(ItemStack stack)
	{
		String name = getItemString(stack);
		if (recipes.get(name) == null)
			return Collections.EMPTY_LIST;
		else
			return recipes.get(name);
	}

	public static NonNullList<ItemStack> getPossibleItems(NonNullList<ItemStack> available)
	{
		RecipeTree subtree = recipeTree.getSubtree(available);

		return subtree.getPossibleStacks();
	}

	public static NonNullList<ItemStack> getPossibleBlocks(NonNullList<ItemStack> available)
	{
		NonNullList<ItemStack> items = getPossibleItems(available);

		NonNullList<ItemStack> blocks = NonNullList.create();

		blocks.addAll(items.stream().filter(stack -> stack.getItem() instanceof ItemBlock).collect(Collectors.toList()));

		return blocks;
	}

	public static RecipeTree getRecipeTree(ItemStack requested, NonNullList<ItemStack> available)
	{
		RecipeTree subtree = recipeTree.getSubtree(available);
		return subtree.getRecipeTree(requested);
	}

	public static RecipeTree getSubTree(NonNullList<ItemStack> available)
	{
		RecipeTree subtree = recipeTree.getSubtree(available);
		return subtree;
	}

	public static String getTierIfStaged(IRecipe recipe)
	{

		if (Loader.isModLoaded("recipestages"))
		{
			try
			{
				Class clazz = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");

				if (clazz.isInstance(recipe))
				{
					Method getTier = ReflectionHelper.findMethod(clazz, "getTier", null);
					String tier = (String) getTier.invoke(recipe);

					return tier;
				}

			} catch (Exception e)
			{
				return "";
			}
		}

		return "";
	}

	public static String getItemString(ItemStack output)
	{
		String outputString = output.getItem().getRegistryName().toString() + "@" + output.getMetadata() + "$" + (output.hasTagCompound() ? output.getTagCompound().toString() : "") + ";";
		return outputString;
	}

	public static String getIngredientString(Ingredient ing)
	{
		StringBuilder sb = new StringBuilder();
		for (ItemStack stack : ing.getMatchingStacks())
			sb.append(getItemString(stack));

		return sb.toString();
	}

	public static ItemStack getItemFromString(String str)
	{
		str = str.substring(0, str.length() - 1);

		int at = str.indexOf('@');
		int hash = str.indexOf('#');
		int dollar = str.indexOf('$');

		String name = str.substring(0, at);
		int meta = Integer.parseInt(str.substring(at + 1, dollar));
		String nbt = dollar == str.length() - 1 ? "" : str.substring(dollar + 1, str.length());

		ItemStack stack = new ItemStack(Item.getByNameOrId(name), 1, meta);

		if (!nbt.isEmpty())
		{
			NBTTagCompound tag;
			try
			{
				tag = JsonToNBT.getTagFromJson(nbt);
				stack.setTagCompound(tag);
			} catch (NBTException e)
			{
				e.printStackTrace();
			}

		}

		return stack;
	}
}
