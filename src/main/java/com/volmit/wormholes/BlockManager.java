package com.volmit.wormholes;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.volmit.wormholes.block.PortalBlock;
import com.volmit.wormholes.util.lang.GMap;
import com.volmit.wormholes.util.lang.GSet;

public class BlockManager implements Listener
{
	private final GMap<Chunk, GSet<PortalBlock>> blocks;

	public BlockManager()
	{
		registerRecipes();
		blocks = new GMap<>();
	}

	public void registerRecipes()
	{
		unregisterAllRecipes();

		//@builder
		registerRecipe(new ShapedRecipe(new NamespacedKey(Wormholes.instance, "portal_wand"), getWand())
				.shape("d d", " r ", " d ")
				.setIngredient('d', Material.GLOWSTONE_DUST)
				.setIngredient('r', Material.BLAZE_ROD));
		registerRecipe(new ShapedRecipe(new NamespacedKey(Wormholes.instance, "portal_rune"), getPortalRune(4))
				.shape("pbp", "bdb", "pbp")
				.setIngredient('d', Material.BLAZE_POWDER)
				.setIngredient('b', Material.PRISMARINE_CRYSTALS)
				.setIngredient('p', Material.ENDER_PEARL));
		registerRecipe(new ShapedRecipe(new NamespacedKey(Wormholes.instance, "wormhole_rune"), getWormholeRune(4))
				.shape("pbp", "bdb", "pbp")
				.setIngredient('d', Material.NETHER_STAR)
				.setIngredient('b', Material.PRISMARINE_SHARD)
				.setIngredient('p', Material.EYE_OF_ENDER));
		registerRecipe(new ShapedRecipe(new NamespacedKey(Wormholes.instance, "portal_chest"), getPortalChest())
				.shape(" d ", "wrw", " e ")
				.setIngredient('d', Material.GLOWSTONE_DUST)
				.setIngredient('w', Material.BLAZE_POWDER)
				.setIngredient('r', Material.ENDER_CHEST)
				.setIngredient('e', Material.EYE_OF_ENDER));
		//@done
	}

	private void registerRecipe(Recipe r)
	{
		if(r instanceof Keyed)
		{
			Keyed k = (Keyed) r;

			try
			{
				Bukkit.addRecipe(r);
				Wormholes.instance.getLogger().info("Registered Recipe: " + k.getKey().toString());
			}

			catch(Throwable e)
			{
				Wormholes.instance.getLogger().warning("Recipe: " + k.getKey().toString() + " is already registered. Skipping registry.");
			}
		}
	}

	private void unregisterAllRecipes()
	{
		Iterator<Recipe> it = Bukkit.getServer().recipeIterator();

		while(it.hasNext())
		{
			Recipe r = it.next();

			if(r instanceof Keyed)
			{
				Keyed k = (Keyed) r;

				if(k.getKey().getKey().equals("wormholes"))
				{
					Wormholes.instance.getLogger().info("Unregistering Recipe: " + k.getKey().toString());
					it.remove();
				}
			}
		}
	}

	public boolean isSame(ItemStack is, ItemStack ib)
	{
		ItemStack a = is.clone();
		ItemStack b = ib.clone();
		a.setAmount(1);
		b.setAmount(1);

		return a.equals(b);
	}

	public ItemStack getWand()
	{
		ItemStack is = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = is.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Portal Wand");
		is.setItemMeta(meta);

		return is;
	}

	public ItemStack getPortalRune(int c)
	{
		ItemStack is = new ItemStack(Material.PRISMARINE);
		ItemMeta meta = is.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Portal Rune");
		is.setItemMeta(meta);
		is.setAmount(c);

		return is;
	}

	public ItemStack getWormholeRune(int c)
	{
		@SuppressWarnings("deprecation")
		ItemStack is = new ItemStack(Material.PRISMARINE, 1, (short) 0, (byte) 2);
		ItemMeta meta = is.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Wormhole Rune");
		is.setItemMeta(meta);
		is.setAmount(c);

		return is;
	}

	public ItemStack getPortalChest()
	{
		ItemStack is = new ItemStack(Material.ENDER_CHEST);
		ItemMeta meta = is.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Portal Chest");
		is.setItemMeta(meta);

		return is;
	}
}
