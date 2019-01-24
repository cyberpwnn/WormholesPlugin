package com.volmit.wormholes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.volmit.wormholes.portal.PortalBlock;
import com.volmit.wormholes.portal.PortalType;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GChunk;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.J;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.S;
import com.volmit.wormholes.util.SR;
import com.volmit.wormholes.util.W;

public class BlockManager implements Listener
{
	private final GMap<GChunk, GSet<PortalBlock>> blocks;

	public BlockManager()
	{
		registerRecipes();
		blocks = new GMap<>();
		J.ar(() -> updatePlacedBlocks(), 9);
	}

	public void destroyAll()
	{
		for(GChunk i : blocks.k())
		{
			try
			{
				destroyAll(i);
			}

			catch(Throwable e)
			{

			}
		}

		blocks.clear();
	}

	@EventHandler
	public void on(ChunkLoadEvent e)
	{
		try
		{
			if(blocks.containsKey(new GChunk(e.getChunk())))
			{
				destroyAll(new GChunk(e.getChunk()));
				J.s(() -> e.getChunk().unload());
			}
		}

		catch(Throwable ex)
		{
			ex.printStackTrace();
		}
	}

	private void destroyAll(GChunk c)
	{
		for(PortalBlock i : blocks.get(c))
		{
			i.getLocation().getBlock().setType(Material.AIR);
			ItemStack is = get(i.getType(), 1);
			i.getLocation().getWorld().dropItemNaturally(i.getLocation().clone().add(0.5, 0.5, 0.5), is);
		}

		blocks.remove(c);
	}

	private void updatePlacedBlocks()
	{
		for(Player i : Bukkit.getOnlinePlayers())
		{
			try
			{
				for(Chunk j : W.chunkRadius(i.getLocation().getChunk(), 2))
				{
					if(blocks.containsKey(new GChunk(j)))
					{
						for(PortalBlock k : blocks.get(new GChunk(j)))
						{
							if(M.r(0.35))
							{
								k.animate(i);
							}
						}
					}
				}
			}

			catch(Throwable e)
			{

			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerInteractEvent e)
	{
		if(isSame(getWand(), e.getPlayer().getItemInHand()) && e.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			PortalBlock b = getBlock(e.getClickedBlock());

			if(b != null)
			{
				if(b.getType().equals(PortalType.PORTAL) || b.getType().equals(PortalType.GATEWAY) || b.getType().equals(PortalType.WORMHOLE))
				{
					construct(e.getPlayer(), e.getClickedBlock());
				}
			}
		}
	}

	private void construct(Player player, Block clickedBlock)
	{
		Set<Block> blocks = new HashSet<>();
		GList<Block> search = new GList<>();
		PortalBlock init = getBlock(clickedBlock);
		PortalType type = init.getType();
		Block cursor = clickedBlock;
		search.addAll(findBlocks(blocks, cursor, type));
		blocks.addAll(search);
		Direction d = Direction.closest(player.getLocation().getDirection());

		new SR(0)
		{
			@Override
			public void run()
			{
				if(M.r(Settings.PORTAL_CONSTRUCT_SPEED))
				{
					for(Block i : new GList<Block>(search))
					{
						if(getBlock(i) == null)
						{
							search.remove(i);
						}
					}

					if(!search.isEmpty())
					{
						search.addAll(findBlocks(blocks, search.popRandom(), type));
					}

					else
					{
						Wormholes.constructionManager.constructPortal(player, blocks, type, d);
						cancel();
					}
				}
			}
		};
	}

	public Set<Block> findBlocks(Set<Block> blocks, Block cursor, PortalType type)
	{
		if(getBlock(cursor) != null)
		{
			blocks.add(cursor);
			cursor.setType(Material.AIR);
			Wormholes.effectManager.playPortalOpening(blocks.size(), cursor);
			removeBlock(getBlock(cursor));
		}

		Set<Block> found = new HashSet<>();

		for(Block i : W.blockFaces(cursor))
		{
			if(!blocks.contains(i) && isBlock(i, type))
			{
				found.add(i);
			}
		}

		return found;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockPlaceEvent e)
	{
		if(isSame(e.getItemInHand(), getPortalRune(1)))
		{
			placeBlock(new PortalBlock(PortalType.PORTAL, e.getBlock().getLocation()));
		}

		else if(isSame(e.getItemInHand(), getWormholeRune(1)))
		{
			placeBlock(new PortalBlock(PortalType.WORMHOLE, e.getBlock().getLocation()));
		}

		else if(isSame(e.getItemInHand(), getGatewayRune(1)))
		{
			placeBlock(new PortalBlock(PortalType.GATEWAY, e.getBlock().getLocation()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockBreakEvent e)
	{
		if(blocks.containsKey(new GChunk(e.getBlock().getLocation().getChunk())))
		{
			ItemStack drop = null;

			for(PortalBlock i : new GList<PortalBlock>(blocks.get(new GChunk(e.getBlock().getLocation().getChunk()))))
			{
				if(i.getLocation().equals(e.getBlock().getLocation()))
				{
					removeBlock(i);
					e.setDropItems(false);

					if(e.getPlayer().getGameMode().equals(GameMode.SURVIVAL))
					{
						switch(i.getType())
						{
							case PORTAL:
								drop = getPortalRune(1);
								break;
							case WORMHOLE:
								drop = getWormholeRune(1);
								break;
							case GATEWAY:
								drop = getGatewayRune(1);
								break;
						}
					}
				}
			}

			if(drop != null)
			{
				ItemStack dr = drop;

				new S()
				{
					@Override
					public void run()
					{
						if(!e.isCancelled() && e.getBlock().isEmpty())
						{
							e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), dr);
						}
					}
				};
			}
		}
	}

	public boolean isBlock(Block block, PortalType type)
	{
		PortalBlock b = getBlock(block);

		if(b == null)
		{
			return false;
		}

		return b.getType().equals(type);
	}

	public PortalBlock getBlock(Block block)
	{
		if(blocks.containsKey(new GChunk(block.getLocation().getChunk())))
		{
			for(PortalBlock i : blocks.get(new GChunk(block.getLocation().getChunk())))
			{
				if(i.getLocation().equals(block.getLocation()))
				{
					return i;
				}
			}
		}

		return null;
	}

	public void removeBlock(PortalBlock block)
	{
		if(blocks.containsKey(new GChunk(block.getLocation().getChunk())))
		{
			blocks.get(new GChunk(block.getLocation().getChunk())).remove(block);

			if(blocks.get(new GChunk(block.getLocation().getChunk())).isEmpty())
			{
				blocks.remove(new GChunk(block.getLocation().getChunk()));
			}

			Wormholes.effectManager.playPortalBlockDestroyed(block.getLocation().getBlock());
		}
	}

	public void placeBlock(PortalBlock block)
	{
		if(!blocks.containsKey(new GChunk(block.getLocation().getChunk())))
		{
			blocks.put(new GChunk(block.getLocation().getChunk()), new GSet<>());
		}

		blocks.get(new GChunk(block.getLocation().getChunk())).add(block);
		Wormholes.effectManager.playPortalBlockPlaced(block.getLocation().getBlock());
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
		if(is == null && ib == null)
		{
			return true;
		}

		if(is == null || ib == null)
		{
			return false;
		}

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

	public ItemStack getGatewayRune(int c)
	{
		@SuppressWarnings("deprecation")
		ItemStack is = new ItemStack(Material.STAINED_GLASS, 1, (short) 0, (byte) 15);
		ItemMeta meta = is.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Gateway Rune");
		is.setItemMeta(meta);
		is.setAmount(c);

		return is;
	}

	public void refund(Set<Block> blocks, PortalType type)
	{
		GList<Block> refund = new GList<Block>(blocks);
		ItemStack is = get(type, 1);

		new SR(0)
		{
			@Override
			public void run()
			{
				if(refund.isEmpty())
				{
					cancel();
					return;
				}

				if(M.r(Settings.PORTAL_COLAPSE_SPEED))
				{
					Block b = refund.pop();
					b.getWorld().dropItemNaturally(b.getLocation().clone().add(0.5, 0.5, 0.5), is);
					Wormholes.effectManager.playPortalFailRefund(b);
				}
			}
		};
	}

	public ItemStack get(PortalType t, int stack)
	{
		switch(t)
		{
			case GATEWAY:
				return getGatewayRune(stack);
			case PORTAL:
				return getPortalRune(stack);
			case WORMHOLE:
				return getWormholeRune(stack);
			default:
				break;
		}

		return null;
	}
}
