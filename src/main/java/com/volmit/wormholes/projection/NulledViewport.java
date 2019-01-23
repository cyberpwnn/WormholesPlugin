package com.volmit.wormholes.projection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.volmit.wormholes.portal.Portal;

public class NulledViewport extends Viewport
{
	public NulledViewport(Player player, Portal portal)
	{
		super(player, portal);
	}
	
	@Override
	public void rebuild()
	{
		
	}
	
	@Override
	public boolean contains(Location l)
	{
		return false;
	}
}
