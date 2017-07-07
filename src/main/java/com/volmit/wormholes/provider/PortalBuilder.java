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
import com.volmit.wormholes.util.P;
import com.volmit.wormholes.util.ParticleEffect;
import com.volmit.wormholes.util.PlayerScrollEvent;
import com.volmit.wormholes.util.Wraith;

public class PortalBuilder implements Listener
{
	private GMap<Player, GBiset<Cuboid, PortalIdentity>> idx;
	private GSet<Player> locks;
	
	public PortalBuilder()
	{
		Wraith.registerListener(this);
		idx = new GMap<Player, GBiset<Cuboid, PortalIdentity>>();
		locks = new GSet<Player>();
	}
	
	public void flush()
	{
		for(Player p : P.onlinePlayers())
		{
			if(isHoldingWand(p))
			{
				if(!new Permissable(p).canBuild())
				{
					return;
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
						
						Cuboid cface = c.getFace(i.f());
						Iterator<Block> it = cface.iterator();
						
						while(it.hasNext())
						{
							Location l = it.next().getLocation();
							ParticleEffect.BARRIER.display(0, 1, l.clone().add(0.5, 0.5, 0.5), 44);
						}
					}
					
					continue;
				}
				
				int size = getSize(p);
				int out = (size - 1) / 2;
				int che = out;
				int height = che * 2;
				Location point = P.targetBlock(p, 24);
				Cuboid c = new Cuboid(point);
				Direction d = Direction.getDirection(p.getLocation().getDirection());
				PortalIdentity pi = new PortalIdentity(d, new PortalKey(DyeColor.BLACK, DyeColor.BLACK, DyeColor.BLACK, DyeColor.BLACK));
				c = c.e(pi.getUp(), height);
				c = c.e(pi.getLeft().getAxis(), out);
				
				for(Direction i : Direction.udnews())
				{
					if(i.getAxis().equals(d.getAxis()))
					{
						continue;
					}
					
					Cuboid cface = c.getFace(i.f());
					Iterator<Block> it = cface.iterator();
					
					while(it.hasNext())
					{
						ParticleEffect.CRIT_MAGIC.display(0.2f, 1, it.next().getLocation().clone().add(0.5, 0.5, 0.5), p);
						ParticleEffect.CRIT.display(0.2f, 1, it.next().getLocation().clone().add(0.5, 0.5, 0.5), p);
					}
				}
				
				idx.put(p, new GBiset<Cuboid, PortalIdentity>(c, pi));
			}
			
			else
			{
				if(locks.contains(p))
				{
					locks.remove(p);
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.35f, 0.45f).play(p);
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.35f, 0.55f).play(p);
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 0.65f, 0.65f).play(p);
					cancelSelect(p);
				}
				
				idx.remove(p);
			}
		}
	}
	
	@EventHandler
	public void on(PlayerInteractEvent e)
	{
		if(isHoldingWand(e.getPlayer()))
		{
			if(!new Permissable(e.getPlayer()).canBuild())
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
						
						Cuboid cface = c.getFace(i.f());
						Iterator<Block> it = cface.iterator();
						
						while(it.hasNext())
						{
							Location l = it.next().getLocation();
							l.getBlock().setType(Material.COAL_BLOCK);
						}
						
						cface.getCenter().getBlock().setType(Material.AIR);
					}
					
					confirm(e.getPlayer());
					new GSound(MSound.ANVIL_USE.bukkitSound(), 1f, 1.9f).play(e.getPlayer());
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 1f, 1.7f).play(e.getPlayer());
					
					locks.remove(e.getPlayer());
				}
				
				else
				{
					new GSound(MSound.ANVIL_LAND.bukkitSound(), 1f, 1.9f).play(e.getPlayer());
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 1f, 1.7f).play(e.getPlayer());
					new GSound(MSound.HORSE_ARMOR.bukkitSound(), 1f, 0.7f).play(e.getPlayer());
					select(e.getPlayer());
					locks.add(e.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void on(PlayerScrollEvent e)
	{
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
		ItemStack is = p.getItemInHand();
		
		return is != null && isWand(is);
	}
	
	public int getSize(Player p)
	{
		if(isHoldingWand(p))
		{
			return getSize(p.getItemInHand());
		}
		
		return -1;
	}
	
	public void giveWand(Player p, int size)
	{
		ItemStack is = p.getItemInHand();
		
		if(isHoldingWand(p))
		{
			p.setItemInHand(getWand(size));
		}
		
		else if(is == null || is.getType().equals(Material.AIR))
		{
			p.setItemInHand(getWand(size));
		}
		
		else
		{
			p.getInventory().addItem(getWand(size));
		}
	}
	
	public GList<String> lore()
	{
		return new GList<String>().qadd(C.GOLD + "Shift + Scroll: " + C.GRAY + "Change portal size").qadd(C.GOLD + "Left Click: " + C.GRAY + "Place frame");
	}
	
	public String nameForSize(int size)
	{
		return C.GOLD + "Portal Wand " + C.DARK_GRAY + " Size: " + C.GOLD + C.UNDERLINE + size;
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
		Wormholes.provider.notifMessage(p, C.GOLD + "Position Selected", C.YELLOW + "Left click to confirm & place");
	}
	
	public void confirm(Player p)
	{
		Wormholes.provider.notifMessage(p, C.GOLD + "Frame Placed", C.YELLOW + " ");
	}
	
	public void cancelSelect(Player p)
	{
		Wormholes.provider.notifMessage(p, C.GOLD + "Position Cancelled", C.YELLOW + " ");
	}
}
