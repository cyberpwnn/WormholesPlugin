package org.cyberpwn.vortex.provider;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cyberpwn.vortex.Status;
import org.cyberpwn.vortex.aperture.VEntity;
import org.cyberpwn.vortex.exception.DuplicatePortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalPositionException;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.portal.PortalIdentity;
import org.cyberpwn.vortex.portal.PortalPosition;
import org.cyberpwn.vortex.service.TimingsService;
import wraith.C;
import wraith.Cuboid;
import wraith.Direction;
import wraith.F;
import wraith.GBiset;
import wraith.GMap;
import wraith.M;
import wraith.P;
import wraith.TXT;
import wraith.TaskLater;
import wraith.Wraith;

public class WandProvider extends BaseProvider implements Listener
{
	private long lastInteraction;
	private GMap<Player, GBiset<Location, Location>> selections;
	
	public WandProvider()
	{
		Wraith.registerListener(this);
		lastInteraction = M.ms();
		selections = new GMap<Player, GBiset<Location, Location>>();
	}
	
	@Override
	public void onFlush()
	{
		
	}
	
	@EventHandler
	public void on(PlayerMoveEvent e)
	{
		if(!e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation()))
		{
			movePlayer(e.getPlayer());
		}
	}
	
	@EventHandler
	public void on(PlayerInteractEvent e)
	{
		if(M.ms() - lastInteraction > 50)
		{
			lastInteraction = M.ms();
			
			if(e.getItem() != null && e.getItem().equals(getWandItem()))
			{
				if(e.getAction().equals(Action.LEFT_CLICK_BLOCK))
				{
					select(e.getPlayer(), e.getClickedBlock().getLocation(), null);
					e.setCancelled(true);
				}
				
				else if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				{
					select(e.getPlayer(), null, e.getClickedBlock().getLocation());
					e.setCancelled(true);
				}
			}
		}
	}
	
	public boolean hasCompleteSelection(Player p)
	{
		return selections.containsKey(p) && selections.get(p).getA() != null && selections.get(p).getB() != null;
	}
	
	public void wipeSelection(Player p)
	{
		selections.remove(p);
	}
	
	public Cuboid getSelection(Player p)
	{
		if(!hasCompleteSelection(p))
		{
			return null;
		}
		
		return new Cuboid(selections.get(p).getA(), selections.get(p).getB());
	}
	
	public void select(Player p, Location a, Location b)
	{
		if(!selections.containsKey(p))
		{
			selections.put(p, new GBiset<Location, Location>(null, null));
		}
		
		if(a != null)
		{
			p.sendMessage(C.LIGHT_PURPLE + "Selected L1");
			selections.get(p).setA(a);
		}
		
		if(b != null)
		{
			p.sendMessage(C.LIGHT_PURPLE + "Selected L2");
			selections.get(p).setB(b);
		}
	}
	
	@EventHandler
	public void on(ServerCommandEvent e)
	{
		if(e.getCommand().equalsIgnoreCase("vpt"))
		{
			e.getSender().sendMessage(C.GREEN + TXT.repeat("-", 12) + "  SYNC " + TXT.repeat("-", 12));
			
			for(String i : TimingsService.root.toLines())
			{
				e.getSender().sendMessage(i);
			}
			
			e.getSender().sendMessage(C.RED + TXT.repeat("-", 12) + " ASYNC " + TXT.repeat("-", 12));
			
			for(String i : TimingsService.asyn.toLines())
			{
				e.getSender().sendMessage(i);
			}
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void on(PlayerCommandPreprocessEvent e)
	{
		if(e.getMessage().equalsIgnoreCase("//totem"))
		{
			e.setCancelled(true);
			ItemStack is = getWandItem();
			e.getPlayer().getInventory().addItem(is);
		}
		
		else if(e.getMessage().equalsIgnoreCase("/d"))
		{
			e.setCancelled(true);
			
			toggleDebugging(e.getPlayer());
		}
		
		else if(e.getMessage().equalsIgnoreCase("/t"))
		{
			e.setCancelled(true);
			
			VEntity ex = new VEntity(e.getPlayer(), EntityType.PLAYER, -6764, UUID.fromString("54defbf6-7712-4864-8c33-85c7e2c8a5ca"), P.targetBlock(e.getPlayer(), 7));
			ex.spawn();
			ex.look((float) Math.random() * 360, (float) Math.random() * 180);
			
			new TaskLater(80)
			{
				@Override
				public void run()
				{
					ex.despawn();
				}
			};
		}
		
		else if(e.getMessage().equalsIgnoreCase("/"))
		{
			e.setCancelled(true);
			
			if(hasCompleteSelection(e.getPlayer()))
			{
				Cuboid c = getSelection(e.getPlayer());
				PortalIdentity i = new PortalIdentity(Direction.getDirection(e.getPlayer().getLocation().getDirection()), null);
				PortalPosition p = new PortalPosition(i, c);
				
				try
				{
					i.setKey(buildKey(p));
					Portal portal = createPortal(i, p);
					e.getPlayer().sendMessage(C.GREEN + "Portal Created " + i.getKey().toString());
					
					if(portal.hasWormhole())
					{
						e.getPlayer().sendMessage(C.LIGHT_PURPLE + "Wormhole Linked");
					}
				}
				
				catch(InvalidPortalPositionException e1)
				{
					e.getPlayer().sendMessage(C.RED + "Invalid Portal Location (Another portal is already here)");
				}
				
				catch(InvalidPortalKeyException ex)
				{
					e.getPlayer().sendMessage(C.RED + "Missing Portal Key (4 colored blocks on frame)");
				}
				
				catch(DuplicatePortalKeyException e1)
				{
					e.getPlayer().sendMessage(C.RED + "2 Portals already contain the key " + i.getKey().toString());
				}
			}
			
			else
			{
				e.getPlayer().sendMessage(C.RED + "Invalid Selection");
			}
		}
	}
	
	public ItemStack getWandItem()
	{
		ItemStack is = new ItemStack(Material.BLAZE_ROD);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(C.LIGHT_PURPLE + "Vortex" + C.GRAY + " Totem");
		is.setItemMeta(im);
		is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		
		return is;
	}
	
	@Override
	public String getDebugMessage()
	{
		return "proj: " + C.GRAY + F.f(Status.projectionTime, 2) + "ms";
	}
}
