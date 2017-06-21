package com.volmit.wormholes.service;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import com.volmit.wormholes.aperture.RemoteInstance;
import com.volmit.wormholes.aperture.RemotePlayer;
import com.volmit.wormholes.aperture.VEntity;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.Timer;
import com.volmit.wormholes.util.Wraith;

public class EntityService implements Listener
{
	private GMap<Player, GMap<Portal, GList<VEntity>>> entities;
	private GMap<Player, GMap<Portal, GSet<Integer>>> aentities;
	
	public EntityService()
	{
		entities = new GMap<Player, GMap<Portal, GList<VEntity>>>();
		aentities = new GMap<Player, GMap<Portal, GSet<Integer>>>();
		Wraith.registerListener(this);
	}
	
	@EventHandler
	public void on(PlayerToggleSneakEvent e)
	{
		for(VEntity i : getAllEntitiesAs(e.getPlayer()))
		{
			i.setSneaking(e.isSneaking());
		}
	}
	
	@EventHandler
	public void on(PlayerInteractEvent e)
	{
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR))
		{
			return;
		}
		
		for(VEntity i : getAllEntitiesAs(e.getPlayer()))
		{
			i.swingArm();
		}
	}
	
	@EventHandler
	public void on(EntityDamageEvent e)
	{
		for(VEntity i : getAllEntitiesAs(e.getEntity()))
		{
			i.takeDamage();
		}
	}
	
	public GList<VEntity> getAllEntitiesAs(Entity e)
	{
		GList<VEntity> vx = new GList<VEntity>();
		int idx = RemoteInstance.create(e).getRemoteId();
		
		for(Player i : entities.k())
		{
			for(Portal j : entities.get(i).k())
			{
				for(VEntity k : entities.get(i).get(j))
				{
					if(k.getId() == idx)
					{
						vx.add(k);
					}
				}
			}
		}
		
		return vx;
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
				if(j.getSided())
				{
					continue;
				}
				
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
		
		UUID id = UUID.randomUUID();
		
		if(ri instanceof RemotePlayer)
		{
			id = ((RemotePlayer) ri).getUuid();
		}
		
		VEntity ve = new VEntity(p, ri.getRemoteType(), ri.getRemoteId(), id, l, ri.getName());
		ve.spawn();
		ve.flush();
		entities.get(p).get(i).add(ve);
	}
	
	public void shutdown()
	{
		flush();
		flush();
	}
}
