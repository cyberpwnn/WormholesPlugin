package com.volmit.wormholes.service;

import java.util.UUID;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.SkinErrorException;
import com.volmit.wormholes.util.SkinProperties;

public class SkinService
{
	private GMap<UUID, SkinProperties> cache;
	private GSet<UUID> request;
	private boolean running;
	
	public SkinService()
	{
		running = false;
		cache = new GMap<UUID, SkinProperties>();
		request = new GSet<UUID>();
	}
	
	public boolean hasProperties(UUID uuid)
	{
		return cache.containsKey(uuid);
	}
	
	public SkinProperties getProperty(UUID uuid)
	{
		if(hasProperties(uuid))
		{
			return cache.get(uuid);
		}
		
		return null;
	}
	
	public void requestProperties(UUID uuid)
	{
		if(hasProperties(uuid))
		{
			return;
		}
		
		request.add(uuid);
	}
	
	public void flush()
	{
		if(running)
		{
			return;
		}
		
		running = true;
		
		try
		{
			new A()
			{
				@Override
				public void async()
				{
					try
					{
						for(UUID i : new GList<UUID>(request))
						{
							try
							{
								SkinProperties s = new SkinProperties(i);
								request.remove(i);
								cache.put(i, s);
								System.out.println("Got skin for " + i);
							}
							
							catch(SkinErrorException e)
							{
								e.printStackTrace();
							}
						}
						
						running = false;
					}
					
					catch(Exception e)
					{
						running = false;
						e.printStackTrace();
					}
				}
			};
		}
		
		catch(Exception e)
		{
			running = false;
			e.printStackTrace();
		}
	}
}
