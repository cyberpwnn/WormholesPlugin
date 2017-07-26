package com.volmit.wormholes.service;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import com.volmit.wormholes.WAPI;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.aperture.RemoteInstance;
import com.volmit.wormholes.aperture.RemotePlayer;
import com.volmit.wormholes.aperture.VEntity;
import com.volmit.wormholes.network.Transmission;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.Execution;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.PlayerScrollEvent;
import com.volmit.wormholes.util.TaskLater;
import com.volmit.wormholes.util.Timer;
import com.volmit.wormholes.util.Wraith;

public class EntityService implements Listener
{
	private GMap<Player, GMap<Portal, GList<VEntity>>> entities;
	private GMap<Player, GMap<Portal, GSet<Integer>>> aentities;
	
	public EntityService()
	{
		DB.d(this, "Starting Entity Service");
		entities = new GMap<Player, GMap<Portal, GList<VEntity>>>();
		aentities = new GMap<Player, GMap<Portal, GSet<Integer>>>();
		Wraith.registerListener(this);
	}
	
	@EventHandler
	public void on(InventoryClickEvent e)
	{
		Player p = (Player) e.getWhoClicked();
		uinv(p);
	}
	
	public void uinv(Player p)
	{
		new TaskLater()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				ItemStack is = p.getItemInHand();
				
				if(is == null)
				{
					is = new ItemStack(Material.AIR, 1, (short) 0, (byte) 0);
				}
				
				MaterialBlock mb = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
				
				boolean b = false;
				
				for(VEntity i : getAllEntitiesAs(p))
				{
					if(i.getVp().setMainHand(is))
					{
						b = true;
					}
				}
				
				if(b)
				{
					dispatchAction(p.getEntityId(), "hand/" + mb.toString());
				}
			}
		};
		
		new TaskLater()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				ItemStack is = p.getInventory().getHelmet();
				
				if(is == null)
				{
					is = new ItemStack(Material.AIR, 1, (short) 0, (byte) 0);
				}
				
				MaterialBlock mb = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
				
				boolean b = false;
				
				for(VEntity i : getAllEntitiesAs(p))
				{
					if(i.getVp().setHelmet(is))
					{
						b = true;
					}
				}
				
				if(b)
				{
					dispatchAction(p.getEntityId(), "ihelm/" + mb.toString());
				}
			}
		};
		
		new TaskLater()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				ItemStack is = p.getInventory().getChestplate();
				
				if(is == null)
				{
					is = new ItemStack(Material.AIR, 1, (short) 0, (byte) 0);
				}
				
				MaterialBlock mb = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
				
				boolean b = false;
				
				for(VEntity i : getAllEntitiesAs(p))
				{
					if(i.getVp().setChestplate(is))
					{
						b = true;
					}
				}
				
				if(b)
				{
					dispatchAction(p.getEntityId(), "ichest/" + mb.toString());
				}
			}
		};
		
		new TaskLater()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				ItemStack is = p.getInventory().getLeggings();
				
				if(is == null)
				{
					is = new ItemStack(Material.AIR, 1, (short) 0, (byte) 0);
				}
				
				MaterialBlock mb = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
				
				boolean b = false;
				
				for(VEntity i : getAllEntitiesAs(p))
				{
					if(i.getVp().setLeggings(is))
					{
						b = true;
					}
				}
				
				if(b)
				{
					dispatchAction(p.getEntityId(), "ilegs/" + mb.toString());
				}
			}
		};
		
		new TaskLater()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				ItemStack is = p.getInventory().getBoots();
				
				if(is == null)
				{
					is = new ItemStack(Material.AIR, 1, (short) 0, (byte) 0);
				}
				
				MaterialBlock mb = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
				
				boolean b = false;
				
				for(VEntity i : getAllEntitiesAs(p))
				{
					if(i.getVp().setBoots(is))
					{
						b = true;
					}
				}
				
				if(b)
				{
					dispatchAction(p.getEntityId(), "iboots/" + mb.toString());
				}
			}
		};
	}
	
	@EventHandler
	public void on(PlayerScrollEvent e)
	{
		new TaskLater()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				ItemStack is = e.getPlayer().getItemInHand();
				
				if(is == null)
				{
					is = new ItemStack(Material.AIR, 1, (short) 0, (byte) 0);
				}
				
				MaterialBlock mb = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
				
				for(VEntity i : getAllEntitiesAs(e.getPlayer()))
				{
					i.getVp().setMainHand(is);
				}
				
				dispatchAction(e.getPlayer().getEntityId(), "hand/" + mb.toString());
			}
		};
	}
	
	@EventHandler
	public void on(PlayerToggleSneakEvent e)
	{
		for(VEntity i : getAllEntitiesAs(e.getPlayer()))
		{
			i.setSneaking(e.isSneaking());
		}
		
		dispatchAction(e.getPlayer().getEntityId(), e.isSneaking() ? "sneak" : "unsneak");
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
		
		dispatchAction(e.getPlayer().getEntityId(), "swn");
	}
	
	@EventHandler
	public void on(EntityDamageEvent e)
	{
		for(VEntity i : getAllEntitiesAs(e.getEntity()))
		{
			i.takeDamage();
		}
		
		if(e.getEntity() instanceof Player)
		{
			dispatchAction(e.getEntity().getEntityId(), "dmg");
		}
	}
	
	public void dispatchAction(int id, String action)
	{
		GSet<String> servers = new GSet<String>();
		DB.d(this, "Dispatch Action: " + id + " -> " + action);
		
		for(Portal i : WAPI.getRemotePortals())
		{
			servers.add(i.getServer());
		}
		
		for(String i : servers)
		{
			Transmission t = new Transmission(Wormholes.bus.getServerName(), i, "action");
			t.set("id", id);
			t.set("ac", action);
			t.send();
		}
	}
	
	public GList<VEntity> getAllEntitiesAs(Entity e)
	{
		GList<VEntity> vx = new GList<VEntity>();
		int idx = RemoteInstance.create(e).getRemoteId();
		
		try
		{
			for(Player i : entities.k())
			{
				for(Portal j : entities.get(i).k())
				{
					try
					{
						for(VEntity k : entities.get(i).get(j))
						{
							if(k.getId() == idx)
							{
								vx.add(k);
							}
						}
					}
					
					catch(Exception ex)
					{
						
					}
				}
			}
		}
		
		catch(Exception xe)
		{
			
		}
		
		return vx;
	}
	
	public GList<VEntity> getAllPlayersAs(int eid)
	{
		GList<VEntity> vx = new GList<VEntity>();
		int idx = 2097800 + eid;
		
		for(Player i : entities.k())
		{
			for(Portal j : entities.get(i).k())
			{
				try
				{
					for(VEntity k : entities.get(i).get(j))
					{
						if(k.getId() == idx)
						{
							vx.add(k);
						}
					}
				}
				
				catch(Exception ex)
				{
					
				}
			}
		}
		
		return vx;
	}
	
	public void flush()
	{
		Wormholes.pool.queue(new Execution()
		{
			@Override
			public void run()
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
									DB.d(this, "Despwn Virtual Entity: " + k.getType() + " <> " + k.getUuid());
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
		});
	}
	
	public void set(Player p, Portal i, RemoteInstance ri, Location l)
	{
		try
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
					uinv(p);
					
					return;
				}
			}
			
			UUID id = UUID.randomUUID();
			
			if(ri instanceof RemotePlayer)
			{
				id = ((RemotePlayer) ri).getUuid();
			}
			
			VEntity ve = new VEntity(p, ri.getRemoteType(), ri.getRemoteId(), id, l, ri.getName());
			DB.d(this, "Spawn Virtual Entity: " + ve.getType() + " <> " + ve.getUuid());
			ve.spawn();
			ve.flush();
			uinv(p);
			entities.get(p).get(i).add(ve);
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void shutdown()
	{
		flush();
		flush();
	}
	
	public int size()
	{
		int x = entities.size();
		
		for(Player i : entities.k())
		{
			for(Portal j : entities.get(i).k())
			{
				x += entities.get(i).get(j).size();
			}
		}
		
		return x;
	}
}
