package com.volmit.wormholes.projection;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;

public class RasteredPlayer
{
	private GMap<Location, MaterialBlock> queuedLayer;
	private GMap<Location, MaterialBlock> ghostLayer;
	private Player p;
	
	public RasteredPlayer(Player p)
	{
		this.p = p;
		queuedLayer = new GMap<Location, MaterialBlock>();
		ghostLayer = new GMap<Location, MaterialBlock>();
	}
	
	public void queue(Location l, MaterialBlock mb)
	{
		queuedLayer.put(l, mb);
	}
	
	public void flush()
	{
		try
		{
			for(Location i : queuedLayer.k())
			{
				MaterialBlock actual = new MaterialBlock(i);
				
				try
				{
					if(queuedLayer.containsKey(i) && actual != null)
					{
						if(queuedLayer.get(i).equals(actual))
						{
							if(!ghostLayer.containsKey(i))
							{
								ghostLayer.put(i, actual);
								continue;
							}
						}
						
						if(ghostLayer.containsKey(i) && ghostLayer.get(i).equals(queuedLayer.get(i)))
						{
							queuedLayer.remove(i);
							continue;
						}
						
						ghostLayer.put(i, queuedLayer.get(i));
					}
				}
				
				catch(Exception e)
				{
					
				}
			}
			
			if(queuedLayer.isEmpty())
			{
				return;
			}
			
			prepareChunks(queuedLayer);
			queuedLayer.clear();
		}
		
		catch(Throwable e)
		{
			
		}
	}
	
	private void prepareChunks(GMap<Location, MaterialBlock> mbx)
	{
		GMap<Chunk, RasteredChunk> preparedChunks = new GMap<Chunk, RasteredChunk>();
		
		for(Location i : mbx.k())
		{
			Chunk c = i.getChunk();
			
			if(!preparedChunks.containsKey(c))
			{
				preparedChunks.put(c, new RasteredChunk(c.getX(), c.getZ(), c.getWorld()));
			}
			
			preparedChunks.get(c).put(i.getBlockX(), i.getBlockY(), i.getBlockZ(), mbx.get(i));
		}
		
		for(Chunk i : preparedChunks.k())
		{
			preparedChunks.get(i).project(p);
		}
	}
	
	public void dequeueAll()
	{
		queuedLayer.clear();
		
		for(Location i : ghostLayer.k())
		{
			queue(i, new MaterialBlock(i));
		}
	}
	
	public boolean isQueued(Location l)
	{
		return queuedLayer.containsKey(l) && !queuedLayer.get(l).equals(new MaterialBlock(l));
	}
	
	public int queueSize()
	{
		return queuedLayer.size();
	}
}
