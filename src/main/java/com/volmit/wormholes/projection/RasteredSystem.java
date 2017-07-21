package com.volmit.wormholes.projection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Status;
import com.volmit.wormholes.service.TimingsService;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.Timer;

public class RasteredSystem
{
	private GMap<Player, RasteredPlayer> rasteredPlayers;
	private boolean flushing;
	private GMap<Player, GList<QueuedChunk>> queueSend;
	
	public RasteredSystem()
	{
		queueSend = new GMap<Player, GList<QueuedChunk>>();
		flushing = false;
		rasteredPlayers = new GMap<Player, RasteredPlayer>();
	}
	
	public void wc(Location c)
	{
		for(Player i : rasteredPlayers.k())
		{
			rasteredPlayers.get(i).wc(c);
		}
	}
	
	public void dequeueAll()
	{
		for(Player i : rasteredPlayers.k())
		{
			rasteredPlayers.get(i).dequeueAll();
		}
	}
	
	public void dequeue(Player p, Location l)
	{
		if(l == null)
		{
			return;
		}
		
		queue(p, l, new MaterialBlock(l));
	}
	
	public boolean isQueued(Player p, Location l)
	{
		if(!rasteredPlayers.containsKey(p))
		{
			rasteredPlayers.put(p, new RasteredPlayer(p));
		}
		
		return rasteredPlayers.get(p).isQueued(l);
	}
	
	public void queue(Player p, Location l, MaterialBlock mb)
	{
		try
		{
			if(!rasteredPlayers.containsKey(p))
			{
				rasteredPlayers.put(p, new RasteredPlayer(p));
			}
			
			if(l == null || mb == null || p == null || !rasteredPlayers.containsKey(p))
			{
				return;
			}
			
			rasteredPlayers.get(p).queue(l, mb);
		}
		
		catch(Exception e)
		{
			
		}
	}
	
	public RasteredPlayer get(Player p)
	{
		return rasteredPlayers.get(p);
	}
	
	public void flushRasterQueue()
	{
		for(Player i : queueSend.k())
		{
			if(queueSend.get(i).isEmpty())
			{
				queueSend.remove(i);
				continue;
			}
			
			int max = 1;
			
			if(queueSend.get(i).size() > Settings.CHUNK_SEND_MAX)
			{
				max++;
			}
			
			if(queueSend.get(i).size() > Settings.CHUNK_SEND_MAX * 2)
			{
				max = Settings.CHUNK_SEND_MAX;
			}
			
			for(int j = 0; j < max; j++)
			{
				if(queueSend.get(i).isEmpty())
				{
					break;
				}
				
				runNext(queueSend.get(i));
			}
		}
	}
	
	private void runNext(GList<QueuedChunk> gList)
	{
		int max = Integer.MAX_VALUE;
		int ind = -1;
		
		for(int i = 0; i < gList.size(); i++)
		{
			QueuedChunk c = gList.get(i);
			
			if(c == null)
			{
				continue;
			}
			
			if(c.getDist() < max)
			{
				max = c.getDist();
				ind = i;
			}
		}
		
		if(ind >= 0)
		{
			QueuedChunk c = gList.get(ind);
			c.run();
			Status.packetBytesPerSecond += c.getBytes();
			gList.remove(ind);
		}
		
	}
	
	public void queueRaster(Player p, QueuedChunk queuedChunk)
	{
		if(!queueSend.containsKey(p))
		{
			queueSend.put(p, new GList<QueuedChunk>());
		}
		
		queueSend.get(p).add(queuedChunk);
	}
	
	public void flush()
	{
		if(flushing)
		{
			return;
		}
		
		Timer t = new Timer();
		t.start();
		
		flushing = true;
		
		for(Player i : rasteredPlayers.k())
		{
			if(i.isOnline())
			{
				rasteredPlayers.get(i).flush();
			}
			
			else
			{
				rasteredPlayers.remove(i);
			}
		}
		
		t.stop();
		TimingsService.asyn.get("mutex-handle").hit("raster-service", t.getTime());
		flushing = false;
	}
}
