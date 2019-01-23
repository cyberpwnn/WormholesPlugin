package com.volmit.wormholes.provider;

import java.util.Iterator;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.volmit.wormholes.Lang;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GBiset;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.GSound;
import com.volmit.wormholes.util.MSound;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.P;
import com.volmit.wormholes.util.PlayerScrollEvent;
import com.volmit.wormholes.util.TaskLater;
import com.volmit.wormholes.util.W;
import com.volmit.wormholes.util.Wraith;
import com.volmit.wormholes.wrapper.WrapperPlayServerSetCooldown;

public class PortalBuilder implements Listener
{
	private GMap<Player, GBiset<Cuboid, PortalIdentity>> idx;
	private GSet<Player> locks;
	private GSet<Player> slock;
	private GMap<Player, GlowingPortal> gp;

	public PortalBuilder()
	{
		Wraith.registerListener(this);
		gp = new GMap<Player, GlowingPortal>();
		idx = new GMap<Player, GBiset<Cuboid, PortalIdentity>>();
		locks = new GSet<Player>();
		slock = new GSet<Player>();
	}

	public void flush()
	{
		if(!Settings.WAND_ENABLED)
		{
			return;
		}

		for(Player p : P.onlinePlayers())
		{
			if(isHoldingWand(p))
			{
				if(!new Permissable(p).canWand())
				{
					continue;
				}

				if(locks.contains(p) && idx.containsKey(p))
				{
					Cuboid c = idx.get(p).getA();
					PortalIdentity pi = idx.get(p).getB();
					Direction d = pi.getBack();

					for(Direction i : Direction.udnews())
					{
						if(i.getAxis().equals(d.getAxis()))
						{
							continue;
						}

						if(gp.containsKey(p))
						{
							gp.get(p).setDone();
							gp.get(p).move(c);
						}
					}

					continue;
				}

				int size = getSize(p);
				int out = (size - 1) / 2;
				int che = out;
				int height = che * 2;
				Location point = P.targetBlock(p, 24);
				int max = 2;
				while(!point.getBlock().getType().isOccluding() && max > 0)
				{
					point = point.subtract(0, 1, 0);
					max--;
				}

				Cuboid c = new Cuboid(point);
				Direction d = Direction.getDirection(p.getLocation().getDirection().normalize());
				PortalIdentity pi = new PortalIdentity(d, new PortalKey(DyeColor.BLACK, DyeColor.BLACK, DyeColor.BLACK, DyeColor.BLACK));
				c = c.e(pi.getUp(), height);
				c = c.e(pi.getLeft().getAxis(), out);
				MaterialBlock mb = new MaterialBlock(Material.COAL_BLOCK);
				MaterialBlock mx = W.getMaterialBlock(Settings.WAND_DEFAULT_MATERIAL);

				if(mx == null)
				{
					mx = mb;
				}

				for(Direction i : Direction.udnews())
				{
					if(i.getAxis().equals(d.getAxis()))
					{
						continue;
					}

					if(!gp.containsKey(p))
					{
						gp.put(p, new GlowingPortal(p, mx.getMaterial()));
					}

					gp.get(p).move(c);
				}

				idx.put(p, new GBiset<Cuboid, PortalIdentity>(c, pi));
			}

			else
			{
				if(gp.containsKey(p))
				{
					gp.get(p).despawn();
					gp.get(p).die();
					gp.remove(p);
				}

				if(locks.contains(p))
				{
					locks.remove(p);
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.15f, 0.45f).play(p);
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.15f, 0.55f).play(p);
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.15f, 0.65f).play(p);
					cancelSelect(p);
				}

				idx.remove(p);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void on(PlayerInteractEvent e)
	{
		if(!Settings.WAND_ENABLED)
		{
			return;
		}

		if(isHoldingWand(e.getPlayer()))
		{
			if(slock.contains(e.getPlayer()))
			{
				return;
			}

			if(!new Permissable(e.getPlayer()).canWand())
			{
				return;
			}

			if(e.getAction().equals(Action.LEFT_CLICK_AIR))
			{
				if(locks.contains(e.getPlayer()))
				{
					Cuboid c = idx.get(e.getPlayer()).getA();
					PortalIdentity pi = idx.get(e.getPlayer()).getB();
					Direction d = pi.getBack();

					for(Direction i : Direction.udnews())
					{
						if(i.getAxis().equals(d.getAxis()))
						{
							continue;
						}

						if(gp.containsKey(e.getPlayer()))
						{
							gp.get(e.getPlayer()).despawn();
							gp.get(e.getPlayer()).die();
							gp.remove(e.getPlayer());
						}

						Cuboid cface = c.getFace(i.f());
						Iterator<Block> it = cface.iterator();
						MaterialBlock mb = new MaterialBlock(Material.COAL_BLOCK);
						MaterialBlock mx = W.getMaterialBlock(Settings.WAND_DEFAULT_MATERIAL);

						if(mx == null)
						{
							mx = mb;
						}

						while(it.hasNext())
						{
							Location l = it.next().getLocation();
							l.getBlock().setType(mx.getMaterial());
							l.getBlock().setData(mx.getData());
						}

						cface.getCenter().getBlock().setType(Material.AIR);
					}

					confirm(e.getPlayer());

					try
					{
						WrapperPlayServerSetCooldown w = new WrapperPlayServerSetCooldown();
						w.setItem(Material.BLAZE_ROD);
						w.setTicks(Settings.WAND_COOLDOWN);
						w.sendPacket(e.getPlayer());
						slock.add(e.getPlayer());

						new TaskLater(Settings.WAND_COOLDOWN)
						{

							@Override
							public void run()
							{
								slock.remove(e.getPlayer());
							}
						};
					}

					catch(Exception ex)
					{

					}

					new GSound(MSound.ANVIL_USE.bukkitSound(), 0.2f, 1.9f).play(e.getPlayer());
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.5f, 1.7f).play(e.getPlayer());

					locks.remove(e.getPlayer());
				}

				else
				{
					new GSound(MSound.ANVIL_LAND.bukkitSound(), 0.3f, 1.9f).play(e.getPlayer());
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.2f, 1.7f).play(e.getPlayer());
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.3f, 0.7f).play(e.getPlayer());
					select(e.getPlayer());
					locks.add(e.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void on(PlayerScrollEvent e)
	{
		if(!Settings.WAND_ENABLED)
		{
			return;
		}

		if(e.getPlayer().isSneaking() && isHoldingWand(e.getPlayer()))
		{
			e.getPlayer().getInventory().setHeldItemSlot(e.getFrom());

			switch(e.getDirection())
			{
				case DOWN:
					changeSize(e.getPlayer(), -4);
				case UP:
					changeSize(e.getPlayer(), 2);
				default:
					break;
			}
		}
	}

	public void changeSize(Player p, int amt)
	{
		if(!Settings.WAND_ENABLED)
		{
			return;
		}

		if(isHoldingWand(p))
		{
			int size = getSize(p);
			int newSize = size + amt;

			if(Settings.MAX_PORTAL_SIZE < newSize)
			{
				newSize = Settings.MAX_PORTAL_SIZE;
			}

			if(newSize < 3)
			{
				newSize = 3;
			}

			giveWand(p, newSize);
		}
	}

	public ItemStack getWand(int size)
	{
		ItemStack is = new ItemStack(Material.BLAZE_ROD);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(nameForSize(size));
		im.setLore(lore());
		is.setItemMeta(im);
		is.addUnsafeEnchantment(Enchantment.DURABILITY, size);

		return is;
	}

	public boolean isHoldingWand(Player p)
	{
		@SuppressWarnings("deprecation")
		ItemStack is = p.getItemInHand();

		return is != null && isWand(is);
	}

	@SuppressWarnings("deprecation")
	public int getSize(Player p)
	{
		if(isHoldingWand(p))
		{
			return getSize(p.getItemInHand());
		}

		return -1;
	}

	@SuppressWarnings("deprecation")
	public void giveWand(Player p, int size)
	{
		if(!Settings.WAND_ENABLED)
		{
			p.sendMessage(C.RED + "Portal Wands disabled.");
			return;
		}

		ItemStack is = p.getItemInHand();

		if(isHoldingWand(p))
		{
			p.setItemInHand(getWand(size));
			new GSound(MSound.WOOD_CLICK.bukkitSound(), 0.3f, 1.9f).play(p);
		}

		else if(is == null || is.getType().equals(Material.AIR))
		{
			p.setItemInHand(getWand(size));
			Wormholes.provider.tipWand(p);
		}

		else
		{
			p.getInventory().addItem(getWand(size));
			Wormholes.provider.tipWand(p);
		}
	}

	public GList<String> lore()
	{
		return new GList<String>().qadd(C.GOLD + Lang.DESCRIPTION_SHIFTSCROLL + ": " + C.GRAY + Lang.DESCRIPTION_CHANGESIZE).qadd(C.GOLD + Lang.DESCRIPTION_LEFTCLICK + ": " + C.GRAY + Lang.DESCRIPTION_PLACEFRAME);
	}

	public String nameForSize(int size)
	{
		return C.GOLD + Lang.DESCRIPTION_PORTALWAND + " " + C.DARK_GRAY + " " + Lang.WORD_SIZE + ": " + C.GOLD + C.UNDERLINE + size;
	}

	public boolean isWand(ItemStack is)
	{
		if(is.getEnchantments().containsKey(Enchantment.DURABILITY) && is.getType().equals(Material.BLAZE_ROD))
		{
			int level = is.getEnchantmentLevel(Enchantment.DURABILITY);
			ItemMeta im = is.getItemMeta();

			if(im.getDisplayName().equals(nameForSize(level)) && im.getLore().size() == lore().size())
			{
				return true;
			}
		}

		return false;
	}

	public int getSize(ItemStack is)
	{
		return is.getEnchantmentLevel(Enchantment.DURABILITY);
	}

	public void select(Player p)
	{
		Wormholes.provider.notifMessage(p, C.GOLD + Lang.DESCRIPTION_POSSELECT, C.YELLOW + Lang.DESCRIPTION_LEFTCLICKCONFIRM);
	}

	public void confirm(Player p)
	{
		Wormholes.provider.notifMessage(p, C.GOLD + Lang.DESCRIPTION_FRAMEPLACED, C.YELLOW + " ");
	}

	public void cancelSelect(Player p)
	{
		Wormholes.provider.notifMessage(p, C.GOLD + Lang.DESCRIPTION_POSCANCEL, C.YELLOW + " ");
	}
}
