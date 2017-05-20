package org.cyberpwn.vortex.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.network.CL;
import org.cyberpwn.vortex.network.Transmission;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.portal.PortalKey;
import org.cyberpwn.vortex.portal.PortalPosition;
import org.cyberpwn.vortex.portal.RemotePortal;
import org.cyberpwn.vortex.wormhole.LocalWormhole;
import org.cyberpwn.vortex.wormhole.MutexWormhole;
import org.cyberpwn.vortex.wormhole.Wormhole;
import wraith.A;
import wraith.CustomGZIPOutputStream;
import wraith.DataCluster;
import wraith.ForwardedPluginMessage;
import wraith.GList;
import wraith.GMap;
import wraith.GQuadraset;
import wraith.JSONObject;
import wraith.TICK;
import wraith.TaskLater;
import wraith.Timer;
import wraith.Wraith;

public class MutexService implements Listener
{
	private GList<Entity> insideThrottle;
	private GMap<UUID, GQuadraset<Portal, Vector, Vector, Vector>> pendingPulls;
	private Integer broadcastInterval;
	
	public MutexService()
	{
		Wraith.registerListener(this);
		insideThrottle = new GList<Entity>();
		pendingPulls = new GMap<UUID, GQuadraset<Portal, Vector, Vector, Vector>>();
		broadcastInterval = 20;
	}
	
	public void setPending(UUID id, Portal portal, Vector velocity, Vector direction, Vector placement)
	{
		pendingPulls.put(id, new GQuadraset<Portal, Vector, Vector, Vector>(portal, velocity, direction, placement));
	}
	
	public void addThrottle(Entity e)
	{
		if(!insideThrottle.contains(e))
		{
			insideThrottle.add(e);
		}
	}
	
	public void removeThrottle(Entity e)
	{
		insideThrottle.remove(e);
	}
	
	public boolean isThrottled(Entity e)
	{
		return insideThrottle.contains(e);
	}
	
	public void addLocalPortal(Portal portal)
	{
		VP.registry.localPortals.add(portal);
		
		for(Chunk i : portal.getPosition().getArea().getChunks())
		{
			i.load();
		}
	}
	
	public void removeLocalPortal(Portal portal)
	{
		VP.registry.localPortals.remove(portal);
	}
	
	public Portal[] getPortals(PortalKey k)
	{
		GList<Portal> p = new GList<Portal>();
		
		for(Portal i : getPortals())
		{
			if(i.getKey().equals(k))
			{
				p.add(i);
			}
		}
		
		return p.toArray(new Portal[p.size()]);
	}
	
	public Portal getPortal(PortalKey k, LocalPortal ignore)
	{
		for(Portal i : getPortals())
		{
			if(i.getKey().equals(k) && !i.equals(ignore))
			{
				return i;
			}
		}
		
		return null;
	}
	
	@EventHandler
	public void on(ChunkUnloadEvent e)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().getChunks().contains(e.getChunk()))
			{
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void on(PlayerMoveEvent e)
	{
		if(!e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation()))
		{
			VP.provider.movePlayer(e.getPlayer());
		}
	}
	
	public boolean hasLink(LocalPortal p)
	{
		return getPortal(p.getKey(), p) != null;
	}
	
	public boolean isLinkMutex(LocalPortal p)
	{
		return hasLink(p) && getPortal(p.getKey(), p) instanceof RemotePortal;
	}
	
	public boolean hasWormhole(LocalPortal p)
	{
		return hasLink(p);
	}
	
	public boolean isMutexWormhole(LocalPortal p)
	{
		return hasLink(p) && isLinkMutex(p);
	}
	
	public Wormhole getWormhole(LocalPortal p)
	{
		if(hasLink(p))
		{
			return isLinkMutex(p) ? new MutexWormhole(p, getPortal(p.getKey(), p)) : new LocalWormhole(p, getPortal(p.getKey(), p));
		}
		
		return null;
	}
	
	public boolean isPositionValid(PortalPosition p)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getPane().equals(p.getPane()))
			{
				return false;
			}
			
			for(Block j : new GList<Block>(i.getPosition().getPane().iterator()))
			{
				if(p.getPane().contains(j))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean isKeyValid(PortalKey k)
	{
		int v = 0;
		
		for(Portal i : getPortals())
		{
			PortalKey r = i.getKey();
			
			if(r.equals(k))
			{
				v++;
				
				if(v > 1)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean isKeyValidAlready(PortalKey k)
	{
		int v = 0;
		
		for(Portal i : getPortals())
		{
			PortalKey r = i.getKey();
			
			if(r.equals(k))
			{
				v++;
				
				if(v > 2)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void flush()
	{
		Timer t = new Timer();
		t.start();
		updatePortals();
		updateThrottles();
		
		try
		{
			broadcastPortals();
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		if(TICK.tick % Settings.NETWORK_POPULATE_MAPPING_INTERVAL == 0 && Settings.PROJECTION_ENABLE)
		{
			for(Portal i : getPortals())
			{
				if(i instanceof RemotePortal && !i.getProjectionPlane().hasContent())
				{
					layer2StreamRequest(i);
				}
			}
		}
		
		t.stop();
		TimingsService.root.get("mutex-handle").hit("mutex-service", t.getTime());
	}
	
	public void dequeue(Portal p)
	{
		VP.registry.destroyQueue.add(p);
	}
	
	private void updateThrottles()
	{
		for(Entity i : insideThrottle.copy())
		{
			boolean b = true;
			
			for(Portal j : getLocalPortals())
			{
				if(j.getPosition().getPane().contains(i.getLocation()))
				{
					b = false;
					break;
				}
			}
			
			if(b)
			{
				removeThrottle(i);
			}
		}
	}
	
	private void broadcastPortals() throws IOException
	{
		if(broadcastInterval > 0)
		{
			broadcastInterval--;
			return;
		}
		
		broadcastInterval = 20;
		
		if(VP.bus.isOnline())
		{
			String name = VP.bus.getServerName();
			
			for(String i : VP.bus.getServers())
			{
				if(!i.equals(name))
				{
					Transmission t = new Transmission(name, i, "id");
					GList<String> portals = new GList<String>();
					
					for(Portal j : getLocalPortals())
					{
						DataCluster data = j.toData();
						String c = data.toJSON().toString();
						portals.add(c);
					}
					
					t.set("p", portals);
					t.send();
				}
			}
			
			for(Transmission i : VP.bus.getInbox())
			{
				if(i.getType().equals("id"))
				{
					VP.bus.read(i);
					
					if(!VP.registry.mutexPortals.containsKey(i.getSource()))
					{
						VP.registry.mutexPortals.put(i.getSource(), new GList<Portal>());
					}
					
					VP.registry.mutexPortals.get(i.getSource()).clear();
					
					for(String j : i.getStringList("p"))
					{
						DataCluster cc = new DataCluster(new JSONObject(j));
						RemotePortal rp = new RemotePortal(i.getSource(), null);
						rp.fromData(cc);
						VP.registry.mutexPortals.get(i.getSource()).add(rp);
					}
				}
				
				else if(i.getType().equals("tp"))
				{
					VP.bus.read(i);
					
					DataCluster cc = new DataCluster(new JSONObject(i.getString("to")));
					
					for(Portal j : VP.registry.localPortals)
					{
						if(cc.equals(j.toData()))
						{
							LocalPortal target = (LocalPortal) j;
							UUID id = UUID.fromString(i.getString("id"));
							Vector velocity = new Vector(i.getDouble("vx"), i.getDouble("vy"), i.getDouble("vz"));
							Vector direction = new Vector(i.getDouble("dx"), i.getDouble("dy"), i.getDouble("dz"));
							Vector entry = new Vector(i.getDouble("ex"), i.getDouble("ey"), i.getDouble("ez"));
							setPending(id, target, velocity, direction, entry);
							break;
						}
					}
				}
				
				else if(i.getType().equals("mreq") && Settings.PROJECTION_ENABLE)
				{
					VP.bus.read(i);
					
					DataCluster cc = new DataCluster(new JSONObject(i.getString("to")));
					
					for(Portal j : VP.registry.localPortals)
					{
						if(cc.equals(j.toData()))
						{
							LocalPortal target = (LocalPortal) j;
							beginStream(target.getServer(), i.getSource(), target.toData().toJSON().toString(), target);
							break;
						}
					}
				}
			}
		}
	}
	
	public void beginStream(String from, String to, String as, LocalPortal lp)
	{
		if(!Settings.PROJECTION_ENABLE)
		{
			return;
		}
		
		if(!lp.getProjectionPlane().hasContent())
		{
			lp.getProjectionPlane().sample(lp.getPosition().getCenter(), Settings.PROJECTION_SAMPLE_RADIUS);
		}
		
		new A()
		{
			@Override
			public void async()
			{
				try
				{
					GList<Byte[]> d = lp.getProjectionPlane().getSuperCompressedByteCull(Settings.NETWORK_MAX_PACKET_SIZE);
					
					for(Byte[] i : d)
					{
						new A()
						{
							@Override
							public void async()
							{
								try
								{
									ByteArrayOutputStream boas = new ByteArrayOutputStream();
									CustomGZIPOutputStream gzi = new CustomGZIPOutputStream(boas);
									DataOutputStream dos = new DataOutputStream(gzi);
									gzi.setLevel(Settings.NETWORK_COMPRESSION_LEVEL);
									dos.writeUTF(as);
									dos.write(ArrayUtils.toPrimitive(i));
									dos.close();
									
									new ForwardedPluginMessage(VP.instance, CL.L2.get(), to, boas).send();
								}
								
								catch(IOException e)
								{
									
								}
							}
						};
					}
				}
				
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		};
	}
	
	public void layer2StreamRequest(Portal remotePortalReference)
	{
		if(!Settings.PROJECTION_ENABLE)
		{
			return;
		}
		
		Transmission r = new Transmission(VP.bus.getServerName(), remotePortalReference.getServer(), "mreq");
		r.set("to", remotePortalReference.toData().toJSON().toString());
		r.send();
	}
	
	public void layer2Stream(byte[] msgbytes)
	{
		if(!Settings.PROJECTION_ENABLE)
		{
			return;
		}
		
		new A()
		{
			@Override
			public void async()
			{
				try
				{
					ByteArrayInputStream bois = new ByteArrayInputStream(msgbytes);
					GZIPInputStream gzi = new GZIPInputStream(bois);
					DataInputStream dis = new DataInputStream(gzi);
					DataCluster c = new DataCluster(new JSONObject(dis.readUTF()));
					byte[] data = IOUtils.toByteArray(dis);
					dis.close();
					
					for(Portal i : getPortals().copy())
					{
						if(i instanceof RemotePortal && i.toData().equals(c))
						{
							i.getProjectionPlane().addSuperCompressed(data);
							break;
						}
					}
				}
				
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		};
	}
	
	public void sendPlayerThrough(UUID id, LocalPortal source, Portal to, Vector velocity, Vector direction, Vector entry)
	{
		if(VP.bus.isOnline())
		{
			String sn = VP.bus.getServerName();
			Transmission t = new Transmission(sn, to.getServer(), "tp");
			t.set("to", to.toData().toJSON().toString());
			t.set("id", id.toString());
			t.set("vx", velocity.getX());
			t.set("vy", velocity.getY());
			t.set("vz", velocity.getZ());
			t.set("dx", direction.getX());
			t.set("dy", direction.getY());
			t.set("dz", direction.getZ());
			t.set("ex", entry.getX());
			t.set("ey", entry.getY());
			t.set("ez", entry.getZ());
			t.forceSend();
		}
	}
	
	private void updatePortals()
	{
		for(Portal i : getLocalPortals())
		{
			i.update();
		}
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		VP.provider.movePlayer(e.getPlayer());
		addThrottle(e.getPlayer());
		
		for(UUID i : pendingPulls.k())
		{
			Player j = e.getPlayer();
			
			if(j.getUniqueId().equals(i))
			{
				GQuadraset<Portal, Vector, Vector, Vector> q = pendingPulls.get(i);
				pendingPulls.remove(i);
				
				if(q.getA() instanceof LocalPortal)
				{
					Location l = q.getA().getPosition().getPane().getCenter().clone().add(q.getD()).clone().setDirection(q.getC());
					addThrottle(j);
					j.teleport(l);
					j.setVelocity(q.getB());
					
					new TaskLater()
					{
						@Override
						public void run()
						{
							j.teleport(l);
							j.setVelocity(q.getB());
						}
					};
				}
				
				break;
			}
		}
	}
	
	public GList<Portal> getPortals()
	{
		GList<Portal> p = new GList<Portal>();
		p.add(VP.registry.localPortals.copy());
		
		for(String i : VP.registry.mutexPortals.k())
		{
			p.add(VP.registry.mutexPortals.get(i).copy());
		}
		
		return p;
	}
	
	public GList<Portal> getLocalPortals()
	{
		return VP.registry.localPortals.copy();
	}
	
	public GList<Entity> getInsideThrottle()
	{
		return insideThrottle;
	}
	
	public Integer getBroadcastInterval()
	{
		return broadcastInterval;
	}
	
	public GMap<String, GList<Portal>> getMutexPortals()
	{
		return VP.registry.mutexPortals;
	}
	
	public GMap<UUID, GQuadraset<Portal, Vector, Vector, Vector>> getPendingPulls()
	{
		return pendingPulls;
	}
}
