package org.cyberpwn.vortex.provider;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.Status;
import org.cyberpwn.vortex.Wormholes;
import org.cyberpwn.vortex.config.Permissable;
import org.cyberpwn.vortex.exception.DuplicatePortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalPositionException;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import wraith.C;
import wraith.Cuboid;
import wraith.Direction;
import wraith.GList;
import wraith.GSound;
import wraith.NMSX;
import wraith.TaskLater;
import wraith.VersionBukkit;
import wraith.W;
import wraith.Wraith;

public class AutomagicalProvider extends BaseProvider implements Listener
{
	public AutomagicalProvider()
	{
		Wraith.registerListener(this);
	}
	
	@Override
	public void onFlush()
	{
		Status.sample();
		
		for(Player i : debug)
		{
			NMSX.sendActionBar(i, Status.inf);
		}
	}
	
	@EventHandler
	public void on(PlayerInteractEvent e)
	{
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			for(Portal i : Wormholes.host.getLocalPortals())
			{
				for(Cuboid j : i.getPosition().getFrame())
				{
					if(j.contains(e.getClickedBlock().getLocation()) && !e.getPlayer().isSneaking())
					{
						if(configure((LocalPortal) i, e.getPlayer()))
						{
							e.setCancelled(true);
						}
						
						return;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void on(PlayerTeleportEvent e)
	{
		if(e.getCause().equals(TeleportCause.ENDER_PEARL))
		{
			if(!new Permissable(e.getPlayer()).canCreate())
			{
				return;
			}
			
			for(Block i : W.blockFaces(e.getTo().getBlock()))
			{
				if(i.getType().equals(Material.GLASS))
				{
					GList<Block> gblock = new GList<Block>();
					gblock.add(i);
					boolean running = true;
					
					while(running)
					{
						boolean a = false;
						
						for(Block j : gblock.copy())
						{
							for(Block k : W.blockFaces(j))
							{
								if(k.getType().equals(Material.GLASS) && !gblock.contains(k))
								{
									a = true;
									gblock.add(k);
								}
							}
						}
						
						if(!a)
						{
							running = false;
						}
						
						if(gblock.size() > Settings.MAX_PORTAL_SIZE * Settings.MAX_PORTAL_SIZE)
						{
							running = false;
						}
					}
					
					if(getBase(Settings.MAX_PORTAL_SIZE).contains(gblock.size()))
					{
						e.setCancelled(true);
						double d = 0.0;
						Block a = null;
						Block b = null;
						
						for(Block j : gblock.copy())
						{
							for(Block k : gblock.copy())
							{
								if(j.equals(k))
								{
									continue;
								}
								
								double dist = k.getLocation().distance(j.getLocation());
								
								if(dist > d)
								{
									d = dist;
									a = k;
									b = j;
								}
							}
						}
						
						Cuboid c = new Cuboid(a.getLocation(), b.getLocation());
						Cuboid dx = new Cuboid(c);
						Direction dir = Direction.getDirection(e.getPlayer().getLocation().getDirection());
						
						for(Direction j : Direction.udnews())
						{
							if(!dir.equals(j) && !dir.reverse().equals(j))
							{
								c = c.expand(j.f(), 1);
							}
						}
						
						try
						{
							LocalPortal p = createPortal(dir, c);
							
							if(VersionBukkit.get().equals(VersionBukkit.V8))
							{
								new GSound(Sound.AMBIENCE_CAVE, 1f, 1.5f).play(p.getPosition().getCenter());
								new GSound(Sound.AMBIENCE_CAVE, 1f, 1.3f).play(p.getPosition().getCenter());
								new GSound(Sound.AMBIENCE_CAVE, 1f, 0.8f).play(p.getPosition().getCenter());
								new GSound(Sound.AMBIENCE_THUNDER, 1f, 1.7f).play(p.getPosition().getCenter());
							}
							
							else
							{
								new GSound(Sound.valueOf("AMBIENT_CAVE"), 1f, 1.5f).play(p.getPosition().getCenter());
								new GSound(Sound.valueOf("AMBIENT_CAVE"), 1f, 1.3f).play(p.getPosition().getCenter());
								new GSound(Sound.valueOf("AMBIENT_CAVE"), 1f, 0.8f).play(p.getPosition().getCenter());
								new GSound(Sound.valueOf("ENTITY_LIGHTNING_THUNDER"), 1f, 1.7f).play(p.getPosition().getCenter());
							}
							
							for(Block j : new GList<Block>(dx.iterator()))
							{
								new TaskLater((int) (Math.random() * 3))
								{
									@SuppressWarnings("deprecation")
									@Override
									public void run()
									{
										j.getWorld().playEffect(j.getLocation(), Effect.TILE_BREAK, j.getTypeId());
										j.breakNaturally();
									}
								};
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
							e.getPlayer().sendMessage(C.RED + "2 Portals already contain this key");
						}
					}
				}
			}
		}
	}
}
