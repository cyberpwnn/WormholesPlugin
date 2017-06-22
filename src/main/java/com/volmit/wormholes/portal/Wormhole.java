package com.volmit.wormholes.portal;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import com.volmit.wormholes.util.GList;

public interface Wormhole
{
	public LocalPortal getSource();
	
	public Portal getDestination();
	
	public void push(Entity e, Location intercept);
	
	public GList<WormholeFilter> getFilters();
}
