package com.volmit.wormholes.projection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	private Queue<Runnable> q;
	
	public RasteredPlayer(Player p)
	{
		this.p = p;
		queuedLayer = new GMap<Location, MaterialBlock>();
		ghostLayer = new GMap<Location, MaterialBlock>();
		q = new ConcurrentLinkedQueue<Runnable>();
	}
	
	public void queue(Location l, MaterialBlock mb)
	{
		q.add(new Runnable()
		{
			@Override
			public void run()
			{
				queuedLayer.put(l, mb);
			}
		});
	}
	
	public void flush()
	{
		try
		{
			while(!q.isEmpty())
			{
				q.poll().run();
			}
			
			for(Location i : queuedLayer.k())
			{
				try
				{
					MaterialBlock actual = new MaterialBlock(i);
					
					try
					{
						if(queuedLayer.containsKey(i) && actual != null)
						{
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
						continue;
					}
				}
				
				catch(Exception e)
				{
					continue;
				}
			}
			
			if(queuedLayer.isEmpty())
			{
				return;
			}
			
			prepareChunks();
		}
		
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	private int prepareChunks()
	{
		GMap<Chunk, RasteredChunk> preparedChunks = new GMap<Chunk, RasteredChunk>();
		
		for(Location i : queuedLayer.k())
		{
			Chunk c = i.getChunk();
			
			if(!preparedChunks.containsKey(c))
			{
				preparedChunks.put(c, new RasteredChunk(c.getX(), c.getZ(), c.getWorld()));
			}
			
			preparedChunks.get(c).put(i.getBlockX(), i.getBlockY(), i.getBlockZ(), queuedLayer.get(i));
			queuedLayer.remove(i);
		}
		
		int k = 0;
		
		for(Chunk i : preparedChunks.k())
		{
			k += preparedChunks.get(i).project(p);
		}
		
		return k;
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
