package org.cyberpwn.vortex.wormhole;

import org.bukkit.entity.Entity;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import wraith.GList;

public interface Wormhole
{
	public LocalPortal getSource();
	
	public Portal getDestination();
	
	public void push(Entity e);
	
	public GList<WormholeFilter> getFilters();
}
