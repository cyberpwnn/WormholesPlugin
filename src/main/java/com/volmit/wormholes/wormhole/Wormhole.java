package com.volmit.wormholes.wormhole;

import org.bukkit.entity.Entity;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import wraith.GList;

public interface Wormhole
{
	public LocalPortal getSource();
	
	public Portal getDestination();
	
	public void push(Entity e);
	
	public GList<WormholeFilter> getFilters();
}
