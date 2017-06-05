package com.volmit.wormholes.provider;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
import wraith.NMSX;
import wraith.ParticleEffect;
import wraith.TaskLater;
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
		if((e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType().equals(Material.AIR)) || e.getPlayer().getItemInHand() == null)
		{
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				for(Portal i : Wormholes.host.getLocalPortals())
				{
					for(Block j : i.getPosition().getKeyBlocks())
					{
						if(j.equals(e.getClickedBlock()))
						{
							if(configure((LocalPortal) i, e.getPlayer()))
							{
								e.setCancelled(true);
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void ona(PlayerInteractEvent e)
	{
		if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			return;
		}
		
		if(e.getPlayer().getItemInHand() == null)
		{
			return;
		}
		
		if(!e.getPlayer().getItemInHand().getType().equals(Material.FLINT_AND_STEEL))
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
		
		Block block = e.getClickedBlock();
		int maxPortalSize = Settings.MAX_PORTAL_SIZE;
		GList<Integer> maxBase = getBaseSqrt(maxPortalSize);
		int setDist = -1;
		Block blockCenter = null;
		Axis axis = null;
		Direction initialD = null;
		Direction altD = null;
		boolean found = false;
		Direction finalDirection = null;
		Cuboid c = null;
		
		for(Direction i : Direction.udnews())
		{
			for(int j : maxBase)
			{
				Block bCheck = block.getLocation().clone().add(i.toVector().clone().multiply(j + 1)).getBlock();
				
				if(W.isColored(bCheck))
				{
					initialD = i;
					setDist = j;
					blockCenter = block.getLocation().clone().add(i.toVector().clone().multiply((j + 1) / 2)).getBlock();
					
					for(Direction k : Direction.udnews())
					{
						if(k.equals(initialD) || k.equals(initialD.reverse()))
						{
							continue;
						}
						
						Block bCheck2 = blockCenter.getLocation().clone().add(k.toVector().clone().multiply((j + 1) / 2)).getBlock();
						
						if(W.isColored(bCheck2))
						{
							altD = k;
							Block bCheck3 = bCheck2.getLocation().clone().add(k.reverse().toVector().clone().multiply(j + 1)).getBlock();
							
							if(W.isColored(bCheck2) && W.isColored(bCheck3))
							{
								GList<Direction> dirs = Direction.udnews();
								dirs.remove(initialD);
								dirs.remove(initialD.reverse());
								dirs.remove(altD);
								dirs.remove(altD.reverse());
								axis = dirs.get(0).getAxis();
								found = true;
							}
						}
						
						if(found)
						{
							break;
						}
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
		
		found = false;
		
		c = new Cuboid(blockCenter.getLocation());
		
		for(Axis i : Axis.values())
		{
			if(i.equals(axis))
			{
				continue;
			}
			
			c = c.e(i, (setDist + 1) / 2);
		}
		
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
		
		Direction md = finalDirection;
		Cuboid cx = c;
		e.setCancelled(true);
		
		new TaskLater()
		{
			@Override
			public void run()
			{
				try
				{
					LocalPortal p = createPortal(md, cx);
					Wormholes.fx.created(p);
				}
				
				catch(InvalidPortalKeyException e1)
				{
					e.setCancelled(true);
					errorMessage(e.getPlayer(), C.RED + "Invalid Portal Key", C.RED + e1.getMessage());
					
					for(Block vc : new GList<Block>(cx.iterator()))
					{
						ParticleEffect.BARRIER.display(0f, 1, vc.getLocation().clone().add(0.5, 0.5, 0.5), 32);
					}
				}
				
				catch(InvalidPortalPositionException e1)
				{
					errorMessage(e.getPlayer(), C.RED + "Invalid Portal Position", C.RED + e1.getMessage());
				}
				
				catch(DuplicatePortalKeyException e1)
				{
					e.setCancelled(true);
					errorMessage(e.getPlayer(), C.RED + "Duplicate Portal Key", C.RED + e1.getMessage());
				}
			}
		};
	}
}
