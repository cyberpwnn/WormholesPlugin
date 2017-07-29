package com.volmit.wormholes.projection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.chunk.NMSChunk10;
import com.volmit.wormholes.chunk.NMSChunk11;
import com.volmit.wormholes.chunk.NMSChunk12;
import com.volmit.wormholes.chunk.NMSChunk19;
import com.volmit.wormholes.chunk.VirtualChunk;
import com.volmit.wormholes.exception.NMSChunkFailureException;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.VersionBukkit;

public class RasteredPlayer
{
	private GMap<Location, MaterialBlock> queuedLayer;
	private GMap<Location, MaterialBlock> ghostLayer;
	private GMap<Chunk, VirtualChunk> virtualChunks;
	private Player p;
	private Queue<Runnable> q;
	
	public RasteredPlayer(Player p)
	{
		this.p = p;
		queuedLayer = new GMap<Location, MaterialBlock>();
		ghostLayer = new GMap<Location, MaterialBlock>();
		q = new ConcurrentLinkedQueue<Runnable>();
		virtualChunks = new GMap<Chunk, VirtualChunk>();
	}
	
	public void trickLight()
	{
		for(Chunk i : virtualChunks.keySet())
		{
			virtualChunks.get(i).trickLight(p);
		}
	}
	
	public void wc(Location c)
	{
		if(virtualChunks.containsKey(c.getChunk()))
		{
			virtualChunks.get(c.getChunk()).set(c.getBlockX() & 15, c.getBlockY(), c.getBlockZ() & 15, new MaterialBlock(c));
		}
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
			flushQueue();
			
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
			
			try
			{
				prepareChunks();
			}
			
			catch(NMSChunkFailureException e)
			{
				if(Settings.USE_LIGHTMAPS)
				{
					Settings.USE_LIGHTMAPS = false;
					prepareChunks();
				}
			}
		}
		
		catch(Throwable e)
		{
			
		}
	}
	
	private void flushQueue()
	{
		while(!q.isEmpty())
		{
			q.poll().run();
		}
	}
	
	private int prepareChunks() throws NMSChunkFailureException
	{
		GMap<Chunk, RasteredChunk> preparedChunks = new GMap<Chunk, RasteredChunk>();
		
		for(Location i : queuedLayer.k())
		{
			Chunk c = i.getChunk();
			VirtualChunk cx = null;
			
			if(!VersionBukkit.wc() && Settings.USE_LIGHTMAPS && !virtualChunks.containsKey(c))
			{
				if(VersionBukkit.get().equals(VersionBukkit.V112))
				{
					cx = new NMSChunk12(c);
				}
				
				else if(VersionBukkit.get().equals(VersionBukkit.V111))
				{
					cx = new NMSChunk11(c);
				}
				
				else if(VersionBukkit.get().equals(VersionBukkit.V11))
				{
					cx = new NMSChunk10(c);
				}
				
				else if(VersionBukkit.get().equals(VersionBukkit.V9))
				{
					cx = new NMSChunk19(c);
				}
				
				if(cx != null)
				{
					virtualChunks.put(c, cx);
				}
			}
			
			if(!preparedChunks.containsKey(c))
			{
				preparedChunks.put(c, new RasteredChunk(c.getX(), c.getZ(), c.getWorld(), virtualChunks.get(c)));
			}
			
			preparedChunks.get(c).put(i.getBlockX(), i.getBlockY(), i.getBlockZ(), queuedLayer.get(i));
			queuedLayer.remove(i);
		}
		
		int k = 0;
		
		for(Chunk i : preparedChunks.k())
		{
			k++;
			preparedChunks.get(i).project(p);
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
