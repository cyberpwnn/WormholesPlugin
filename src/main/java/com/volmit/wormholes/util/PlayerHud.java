package com.volmit.wormholes.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class PlayerHud extends BaseHud
{
	private boolean closeOnMove;
	private Location last;
	private Location in;
	private double maxDist;
	
	public PlayerHud(Player player, boolean closeOnMove)
	{
		super(player);
		
		this.closeOnMove = closeOnMove;
		last = null;
		in = player.getLocation();
		maxDist = 3.4;
	}
	
	public PlayerHud(Player player)
	{
		this(player, true);
	}
	
	@Override
	public Location getBaseLocation()
	{
		Location host = P.getHand(player, 0f, 0f).clone().add(0, -3, 0).add(player.getLocation().getDirection().clone().multiply(2));
		Vector left = VectorMath.angleLeft(player.getLocation().getDirection(), 90).clone().multiply(index);
		
		return host.clone().add(left);
	}
	
	@Override
	public void onUpdateInternal()
	{
		holo.setLocation(getBaseLocation());
		
		if(closeOnMove)
		{
			if(last != null)
			{
				if(!player.getLocation().getBlock().getLocation().equals(last.getBlock().getLocation()))
				{
					if(player.getLocation().distanceSquared(in) > maxDist)
					{
						close();
					}
				}
			}
			
			last = player.getLocation();
		}
	}
}
