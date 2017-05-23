package org.cyberpwn.vortex.wormhole;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import wraith.GList;

public abstract class BaseWormhole implements Wormhole
{
	private LocalPortal source;
	private Portal destination;
	private GList<WormholeFilter> filters;
	
	public BaseWormhole(LocalPortal source, Portal destination)
	{
		this.source = source;
		this.destination = destination;
		filters = new GList<WormholeFilter>();
	}
	
	@Override
	public LocalPortal getSource()
	{
		return source;
	}
	
	@Override
	public Portal getDestination()
	{
		return destination;
	}
	
	@Override
	public GList<WormholeFilter> getFilters()
	{
		return filters;
	}
	
	@Override
	public void push(Entity e)
	{
		for(WormholeFilter i : getFilters())
		{
			if(i.onFilter(this, e))
			{
				VP.fx.throwBack(e, e.getVelocity().clone().add(new Vector(0, 1, 0)).clone().multiply(1.7), getSource());
				return;
			}
		}
		
		VP.fx.push(e, e.getVelocity(), getSource());
		onPush(e);
	}
	
	public abstract void onPush(Entity e);
}
