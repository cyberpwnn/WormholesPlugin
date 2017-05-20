package org.cyberpwn.vortex.service;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.aperture.RemoteInstance;
import org.cyberpwn.vortex.aperture.VEntity;
import org.cyberpwn.vortex.portal.Portal;
import wraith.GList;
import wraith.GMap;
import wraith.GSet;
import wraith.Timer;

public class EntityService
{
	private GMap<Player, GMap<Portal, GList<VEntity>>> entities;
	private GMap<Player, GMap<Portal, GSet<Integer>>> aentities;
	
	public EntityService()
	{
		entities = new GMap<Player, GMap<Portal, GList<VEntity>>>();
		aentities = new GMap<Player, GMap<Portal, GSet<Integer>>>();
	}
	
	public void flush()
	{
		Timer t = new Timer();
		t.start();
		
		for(Player i : entities.k())
		{
			if(!i.isOnline())
			{
				entities.remove(i);
				continue;
			}
			
			for(Portal j : entities.get(i).k())
			{
				if(aentities.containsKey(i) && aentities.get(i).containsKey(j))
				{
					for(VEntity k : entities.get(i).get(j).copy())
					{
						if(!aentities.get(i).get(j).contains(k.getId()))
						{
							k.despawn();
							entities.get(i).get(j).remove(k);
						}
						
						else
						{
							k.flush();
							aentities.get(i).get(j).remove(k.getId());
						}
					}
				}
			}
		}
		
		t.stop();
		TimingsService.root.get("capture-manager").get("aperture-service").hit("entity-service", t.getTime());
	}
	
	public void set(Player p, Portal i, RemoteInstance ri, Location l)
	{
		if(!entities.containsKey(p))
		{
			entities.put(p, new GMap<Portal, GList<VEntity>>());
		}
		
		if(!entities.get(p).containsKey(i))
		{
			entities.get(p).put(i, new GList<VEntity>());
		}
		
		if(!aentities.containsKey(p))
		{
			aentities.put(p, new GMap<Portal, GSet<Integer>>());
		}
		
		if(!aentities.get(p).containsKey(i))
		{
			aentities.get(p).put(i, new GSet<Integer>());
		}
		
		aentities.get(p).get(i).add(ri.getRemoteId());
		
		for(VEntity e : entities.get(p).get(i).copy())
		{
			if(ri.getRemoteId() == e.getId())
			{
				e.teleport(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
				e.flush();
				return;
			}
		}
		
		VEntity ve = new VEntity(p, ri.getRemoteType(), ri.getRemoteId(), UUID.randomUUID(), l);
		ve.spawn();
		ve.flush();
		entities.get(p).get(i).add(ve);
	}
}
