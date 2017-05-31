package com.volmit.wormholes.provider;

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
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Status;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.exception.DuplicatePortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalPositionException;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import wraith.Axis;
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
	private boolean pearl = false;
	
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
	public void ona(PlayerInteractEvent e)
	{
		if(pearl)
		{
			return;
		}
		
		if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}
		
		if(!new Permissable(e.getPlayer()).canCreate())
		{
			return;
		}
		
		if(!W.isColored(e.getClickedBlock()))
		{
			return;
		}
		
		// Define shit
		Block block = e.getClickedBlock();
		int maxPortalSize = Settings.MAX_PORTAL_SIZE;
		int maxAirSize = maxPortalSize - 2;
		int maxPortalArea = (int) Math.pow(maxPortalSize, 2);
		int maxAirArea = (int) Math.pow(maxAirSize, 2);
		GList<Integer> maxBase = getBaseSqrt(maxPortalSize);
		int setDist = -1;
		Block blockPerp = null;
		Block blockCenter = null;
		Axis axis = null;
		Direction initialD = null;
		Direction altD = null;
		boolean found = false;
		Block bAl = null;
		Block bBe = null;
		Direction finalDirection = null;
		Cuboid c = null;
		
		// Try and find another colorable block from the block lit on fire
		for(Direction i : Direction.udnews())
		{
			for(int j : maxBase)
			{
				//Check the block
				Block bCheck = block.getLocation().clone().add(i.toVector().clone().multiply(j + 1)).getBlock();
				
				if(W.isColored(bCheck))
				{
					//We found a block in a valid position that is colored
					blockPerp = bCheck;
					initialD = i;
					setDist = j;
					found = true;
					blockCenter = block.getLocation().clone().add(i.toVector().clone().multiply((j + 1) / 2)).getBlock();
					break;
				}
			}
			
			if(found)
			{
				break;
			}
		}
		
		if(!found)
		{
			return;
		}
		
		found = false;
		
		//Find Next Position perpendicular to the two already found using the center block
		for(Direction i : Direction.udnews())
		{
			// Ignore initialD
			if(i.equals(initialD) || i.equals(initialD.reverse()))
			{
				continue;
			}
			
			for(int j : maxBase)
			{
				// From the center, cross over half the base max to check for color
				Block bCheck = blockCenter.getLocation().clone().add(i.toVector().clone().multiply((j + 1) / 2)).getBlock();
				
				if(W.isColored(bCheck))
				{
					// Found a colored block
					bAl = bCheck;
					altD = i;
					
					// Check the final block to ensure it's colored
					bCheck = bAl.getLocation().clone().add(i.reverse().toVector().clone().multiply(j + 1)).getBlock();
					
					if(W.isColored(bCheck))
					{
						// Correct, the fourth block is colored. We have a portal ladies.
						bBe = bCheck;
						
						// Define axis
						GList<Direction> dirs = Direction.udnews();
						dirs.remove(initialD);
						dirs.remove(initialD.reverse());
						dirs.remove(altD);
						dirs.remove(altD.reverse());
						axis = dirs.get(0).getAxis();
						found = true;
						break;
					}
				}
			}
			
			if(found)
			{
				break;
			}
		}
		
		if(!found)
		{
			return;
		}
		
		//Assemble Pane
		c = new Cuboid(blockCenter.getLocation());
		
		for(Axis i : Axis.values())
		{
			if(i.equals(axis))
			{
				continue;
			}
			
			//Expand the cuboid half the width+1
			c = c.e(i, (setDist + 1) / 2);
		}
		
		// Pick a correct direction
		GList<Direction> dirs = Direction.udnews();
		dirs.remove(initialD);
		dirs.remove(initialD.reverse());
		dirs.remove(altD);
		dirs.remove(altD.reverse());
		double maxDist = Double.MAX_VALUE;
		
		for(Direction i : dirs)
		{
			double dist = i.toVector().distance(e.getPlayer().getLocation().getDirection());
			
			if(dist < maxDist)
			{
				maxDist = dist;
				finalDirection = i;
			}
		}
		
		//Assemble Portal
		try
		{
			LocalPortal p = createPortal(finalDirection, c);
			
			// Portal created without issues, cancel event
			e.setCancelled(true);
			
			// SPECIAL EFFECTS
			if(VersionBukkit.get().equals(VersionBukkit.V8))
			{
				// 1.8 fx
				new GSound(Sound.AMBIENCE_CAVE, 1f, 1.5f).play(p.getPosition().getCenter());
				new GSound(Sound.AMBIENCE_CAVE, 1f, 1.3f).play(p.getPosition().getCenter());
				new GSound(Sound.AMBIENCE_CAVE, 1f, 0.8f).play(p.getPosition().getCenter());
				new GSound(Sound.AMBIENCE_THUNDER, 1f, 1.7f).play(p.getPosition().getCenter());
			}
			
			else
			{
				// 1.9+ fx
				new GSound(Sound.valueOf("AMBIENT_CAVE"), 1f, 1.5f).play(p.getPosition().getCenter());
				new GSound(Sound.valueOf("AMBIENT_CAVE"), 1f, 1.3f).play(p.getPosition().getCenter());
				new GSound(Sound.valueOf("AMBIENT_CAVE"), 1f, 0.8f).play(p.getPosition().getCenter());
				new GSound(Sound.valueOf("ENTITY_LIGHTNING_THUNDER"), 1f, 1.7f).play(p.getPosition().getCenter());
			}
		}
		
		catch(InvalidPortalKeyException e1)
		{
			// There are already two portals with this key
			e.setCancelled(true);
			e.getPlayer().sendMessage(C.RED + e1.getMessage());
		}
		
		catch(InvalidPortalPositionException e1)
		{
			// Not a portal?
			System.out.println("dbg");
		}
		
		catch(DuplicatePortalKeyException e1)
		{
			// There are already two portals with this key
			e.setCancelled(true);
			e.getPlayer().sendMessage(C.RED + e1.getMessage());
		}
	}
	
	@EventHandler
	public void on(PlayerTeleportEvent e)
	{
		if(!pearl)
		{
			return;
		}
		
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
