package com.volmit.wormholes.projection;

import org.bukkit.Location;
import org.bukkit.World;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;

public class RasteredServer
{
	private GMap<World, RasteredWorld> projections;
	
	public RasteredServer()
	{
		projections = new GMap<World, RasteredWorld>();
	}
	
	public void queue(Location l, MaterialBlock b)
	{
		if(!projections.containsKey(l.getWorld()))
		{
			projections.put(l.getWorld(), new RasteredWorld(l.getWorld()));
		}
		
		projections.get(l.getWorld()).queue(l, b);
	}
	
	public void flush()
	{
		for(World i : projections.k())
		{
			projections.get(i).flush();
		}
	}
	
	public boolean canFlush()
	{
		for(World i : projections.k())
		{
			if(projections.get(i).hasChunks())
			{
				return true;
			}
		}
		
		return false;
	}
}
