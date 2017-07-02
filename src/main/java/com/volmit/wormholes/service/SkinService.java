package com.volmit.wormholes.service;

import java.io.File;
import java.util.UUID;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.SkinErrorException;
import com.volmit.wormholes.util.SkinProperties;

public class SkinService
{
	private GMap<UUID, SkinProperties> cache;
	private GSet<UUID> request;
	private boolean running;
	
	public SkinService()
	{
		DB.d(this, "Starting Skin Service");
		running = false;
		cache = new GMap<UUID, SkinProperties>();
		request = new GSet<UUID>();
		runPurger();
	}
	
	public void runPurger()
	{
		long msx = Settings.SKIN_CACHE_PURGE_THRESHOLD;
		msx = msx * 24l * 60l * 60l * 1000l;
		long mb = msx;
		
		new A()
		{
			@Override
			public void async()
			{
				File f = new File(Wormholes.instance.getDataFolder(), "cache");
				File fx = new File(f, "skins");
				
				if(!fx.exists())
				{
					return;
				}
				
				for(File i : fx.listFiles())
				{
					if(M.ms() - i.lastModified() > mb)
					{
						DB.d(this, "Purging skin cache file " + i);
						i.delete();
					}
				}
			}
		};
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
								SkinProperties s = null;
								DB.d(this, "Skin Request for " + i);
								if(Wormholes.io.hasSkin(i))
								{
									DB.d(this, "Loaded Cached Skin " + i + " from disk.");
									s = Wormholes.io.loadSkin(i);
								}
								
								else
								{
									DB.d(this, "Downloading skin " + i);
									s = new SkinProperties(i);
								}
								
								request.remove(i);
								DB.d(this, "Cached Skin file " + i);
								cache.put(i, s);
								
								if(!Wormholes.io.hasSkin(i))
								{
									DB.d(this, "Saved Dcache skin " + i);
									Wormholes.io.saveSkin(i, s);
								}
							}
							
							catch(SkinErrorException e)
							{
								DB.d(this, "Failed to download skin " + i + ". Waiting for cooldown on mojang server...");
							}
						}
						
						running = false;
					}
					
					catch(Exception e)
					{
						running = false;
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
