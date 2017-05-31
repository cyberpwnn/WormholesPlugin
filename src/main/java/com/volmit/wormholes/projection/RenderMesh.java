package com.volmit.wormholes.projection;

import org.bukkit.entity.Player;
import com.volmit.wormholes.portal.LocalPortal;
import wraith.GMap;

public class RenderMesh
{
	private GMap<LocalPortal, RenderSet> set;
	
	public RenderMesh()
	{
		set = new GMap<LocalPortal, RenderSet>();
	}
	
	public PartialRenderer getRenderer(Player p, LocalPortal l, Viewport v)
	{
		if(!set.containsKey(l))
		{
			set.put(l, new RenderSet(l));
		}
		
		return set.get(l).getRenderer(v, p);
	}
	
	public void removePortal(LocalPortal p)
	{
		set.remove(p);
	}
}
