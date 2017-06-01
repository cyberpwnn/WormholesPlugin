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
		Block bAl = null;
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
		
		for(Direction i : Direction.udnews())
		{
			if(i.equals(initialD) || i.equals(initialD.reverse()))
			{
				continue;
			}
			
			for(int j : maxBase)
			{
				Block bCheck = blockCenter.getLocation().clone().add(i.toVector().clone().multiply((j + 1) / 2)).getBlock();
				
				if(W.isColored(bCheck))
				{
					bAl = bCheck;
					altD = i;
					bCheck = bAl.getLocation().clone().add(i.reverse().toVector().clone().multiply(j + 1)).getBlock();
					
					if(W.isColored(bCheck))
					{
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
					e.getPlayer().sendMessage(C.RED + e1.getMessage());
				}
				
				catch(InvalidPortalPositionException e1)
				{
					System.out.println("dbg");
				}
				
				catch(DuplicatePortalKeyException e1)
				{
					e.setCancelled(true);
					e.getPlayer().sendMessage(C.RED + e1.getMessage());
				}
			}
		};
	}
}
