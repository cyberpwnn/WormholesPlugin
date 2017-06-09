package com.volmit.wormholes.projection;

import org.bukkit.entity.Player;
import com.volmit.wormholes.util.GList;

public class ProjectionMask
{
	private GList<Player> needsProjection;
	
	public ProjectionMask()
	{
		needsProjection = new GList<Player>();
	}
	
	public void projected(Player p)
	{
		needsProjection.remove(p);
	}
	
	public void sched(Player p)
	{
		if(!needsProjection.contains(p))
		{
			needsProjection.add(p);
		}
	}
	
	public boolean needsProjection()
	{
		return !needsProjection.isEmpty();
	}
	
	public void clear()
	{
		needsProjection.clear();
	}
}
