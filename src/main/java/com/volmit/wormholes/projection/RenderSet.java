package com.volmit.wormholes.projection;

import org.bukkit.entity.Player;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.util.GMap;

public class RenderSet
{
	private LocalPortal portal;
	private GMap<Player, PartialRenderer> renderers;
	
	public RenderSet(LocalPortal portal)
	{
		this.portal = portal;
		renderers = new GMap<Player, PartialRenderer>();
	}
	
	public LocalPortal getPortal()
	{
		return portal;
	}
	
	public GMap<Player, PartialRenderer> getRenderers()
	{
		return renderers;
	}
	
	public PartialRenderer getRenderer(Viewport v, Player p)
	{
		if(!getRenderers().containsKey(p) || !getRenderers().get(p).getView().equals(v) || getRenderers().get(p).isComplete())
		{
			getRenderers().put(p, new PartialRenderer(v));
		}
		
		return getRenderers().get(p);
	}
	
	public void update()
	{
		for(Player i : getRenderers().k())
		{
			if(!i.isOnline())
			{
				getRenderers().remove(i);
			}
		}
	}
}
