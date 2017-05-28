package org.cyberpwn.vortex.projection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.Status;
import org.cyberpwn.vortex.service.TimingsService;
import wraith.GList;
import wraith.GMap;
import wraith.MaterialBlock;
import wraith.Timer;

public class RasteredSystem
{
	private GMap<Player, RasteredPlayer> rasteredPlayers;
	private boolean flushing;
	private GMap<Player, GList<Runnable>> queueSend;
	
	public RasteredSystem()
	{
		queueSend = new GMap<Player, GList<Runnable>>();
		flushing = false;
		rasteredPlayers = new GMap<Player, RasteredPlayer>();
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
		if(!rasteredPlayers.containsKey(p))
		{
			rasteredPlayers.put(p, new RasteredPlayer(p));
		}
		
		rasteredPlayers.get(p).queue(l, mb);
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
				
				queueSend.get(i).pop().run();
			}
		}
	}
	
	public void queueRaster(Player p, Runnable r)
	{
		if(!queueSend.containsKey(p))
		{
			queueSend.put(p, new GList<Runnable>());
		}
		
		queueSend.get(p).add(r);
	}
	
	public void flush()
	{
		Status.avgBPS.put(Status.packetBytesPerSecond);
		Status.packetBytesPerSecond = 0;
		
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
