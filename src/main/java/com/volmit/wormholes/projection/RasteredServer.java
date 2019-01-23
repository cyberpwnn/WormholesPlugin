package com.volmit.wormholes.projection;

import org.bukkit.World;
import com.volmit.wormholes.util.GMap;

public class RasteredServer
{
	private GMap<World, RasteredWorld> projections;
	
	public RasteredServer()
	{
		projections = new GMap<World, RasteredWorld>();
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
