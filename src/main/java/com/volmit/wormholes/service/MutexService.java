package com.volmit.wormholes.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Status;
import com.volmit.wormholes.WAPI;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.aperture.EntityHider;
import com.volmit.wormholes.aperture.EntityHider.Policy;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.network.CL;
import com.volmit.wormholes.network.Transmission;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.portal.PortalPosition;
import com.volmit.wormholes.portal.RemotePortal;
import com.volmit.wormholes.projection.ArrivalVector;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.CustomGZIPOutputStream;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.EntityHologram;
import com.volmit.wormholes.util.FinalInteger;
import com.volmit.wormholes.util.ForwardedPluginMessage;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GQuadraset;
import com.volmit.wormholes.util.GSound;
import com.volmit.wormholes.util.JSONObject;
import com.volmit.wormholes.util.MSound;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.PlayerScrollEvent;
import com.volmit.wormholes.util.TICK;
import com.volmit.wormholes.util.Task;
import com.volmit.wormholes.util.TaskLater;
import com.volmit.wormholes.util.Timer;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.W;
import com.volmit.wormholes.util.Wraith;
import com.volmit.wormholes.wormhole.LocalWormhole;
import com.volmit.wormholes.wormhole.MutexWormhole;
import com.volmit.wormholes.wormhole.Wormhole;

public class MutexService implements Listener
{
	private GList<Entity> insideThrottle;
	private GMap<UUID, GQuadraset<Portal, Vector, Vector, Vector>> pendingPulls;
	private Integer broadcastInterval;
	private GMap<Player, Runnable> waiting;
	private GMap<Player, Vector> actualVectors;
	private GMap<LocalPortal, GMap<UUID, ArrivalVector>> arrivals;
	private GList<Integer> iLock;
	private EntityHider hider;
	
	public MutexService()
	{
		Wraith.registerListener(this);
		insideThrottle = new GList<Entity>();
		waiting = new GMap<Player, Runnable>();
		pendingPulls = new GMap<UUID, GQuadraset<Portal, Vector, Vector, Vector>>();
		broadcastInterval = 20;
		arrivals = new GMap<LocalPortal, GMap<UUID, ArrivalVector>>();
		iLock = new GList<Integer>();
		actualVectors = new GMap<Player, Vector>();
		hider = new EntityHider(Wormholes.instance, Policy.BLACKLIST);
	}
	
	public void sendArrival(RemotePortal r, Player p, ArrivalVector v)
	{
		Transmission t = new Transmission(Wormholes.bus.getServerName(), r.getServer(), "a");
		t.set("id", p.getUniqueId().toString());
		t.set("v", v.toString());
		t.set("rid", r.toData().toJSON().toString());
		t.send();
	}
	
	public void ilock(Entity e)
	{
		if(isILocked(e))
		{
			return;
		}
		
		iLock.add(e.getEntityId());
	}
	
	public boolean isILocked(Entity e)
	{
		return iLock.contains(e.getEntityId());
	}
	
	public void unILock(Entity e)
	{
		iLock.remove((Integer) e.getEntityId());
	}
	
	public ArrivalVector getArrival(LocalPortal l, Player p)
	{
		return arrivals.containsKey(l) ? arrivals.get(l).get(p.getUniqueId()) : null;
	}
	
	public boolean hasArrival(LocalPortal l, Player p)
	{
		return getArrival(l, p) != null;
	}
	
	public void setArrival(LocalPortal p, UUID u, ArrivalVector a)
	{
		if(!arrivals.containsKey(p))
		{
			arrivals.put(p, new GMap<UUID, ArrivalVector>());
		}
		
		arrivals.get(p).put(u, a);
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
	
	public void setDestinationSided(LocalPortal p, boolean sided)
	{
		if(p.hasWormhole())
		{
			if(p.isWormholeMutex())
			{
				Transmission t = new Transmission(p.getServer(), p.getWormhole().getDestination().getServer(), sided ? "sided" : "usided");
				t.set("id", p.getWormhole().getDestination().toData().toJSON().toString());
				((RemotePortal) p.getWormhole().getDestination()).setWait();
				t.send();
				
				try
				{
					broadcastPortals();
				}
				
				catch(IOException e)
				{
					
				}
			}
			
			else
			{
				((LocalPortal) p.getWormhole().getDestination()).setSided(sided);
			}
		}
	}
	
	public void on(EntitySpawnEvent e)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(e.getLocation()))
			{
				((LocalPortal) i).checkSend(e.getEntity(), i.getWormhole(), e.getLocation());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerItemHeldEvent e)
	{
		Wraith.callEvent(new PlayerScrollEvent(e.getPlayer(), e.getPreviousSlot(), e.getNewSlot()));
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
		Wormholes.registry.localPortals.add(portal);
		
		for(Chunk i : portal.getPosition().getArea().getChunks())
		{
			i.load();
		}
		
		Wormholes.projector.deproject((LocalPortal) portal);
	}
	
	public void removeLocalPortal(Portal portal)
	{
		((LocalPortal) portal).clearHolograms();
		((LocalPortal) portal).destroy();
		Wormholes.projector.getMesh().removePortal((LocalPortal) portal);
		Wormholes.projector.deproject((LocalPortal) portal);
		Wormholes.registry.localPortals.remove(portal);
		Wormholes.provider.wipe((LocalPortal) portal);
	}
	
	public void removeLocalPortalReverse(Portal portal)
	{
		((LocalPortal) portal).clearHolograms();
		Wormholes.projector.getMesh().removePortal((LocalPortal) portal);
		Wormholes.projector.deproject((LocalPortal) portal);
		Wormholes.registry.localPortals.remove(portal);
		Wormholes.provider.wipe((LocalPortal) portal);
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
		if(e.getFrom().getWorld().equals(e.getTo().getWorld()))
		{
			actualVectors.put(e.getPlayer(), VectorMath.directionNoNormal(e.getFrom(), e.getTo()));
		}
		
		if(e.getFrom().getX() != e.getTo().getX() || e.getFrom().getY() != e.getTo().getY() || e.getFrom().getZ() != e.getTo().getZ())
		{
			Wormholes.provider.movePlayer(e.getPlayer());
		}
		
		if(e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ())
		{
			handleArrivalIntent(e.getPlayer());
		}
		
		for(Portal i : WAPI.getLocalPortals())
		{
			if(i.getPosition().getArea().contains(e.getTo()))
			{
				if(!e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation()))
				{
					((LocalPortal) i).getMask().sched(e.getPlayer());
				}
			}
		}
	}
	
	private void handleArrivalIntent(Player e)
	{
		for(Portal portal : Wormholes.registry.getPortalsInCloseView(e.getLocation()))
		{
			if(portal != null && portal.hasWormhole() && portal.isWormholeMutex())
			{
				Portal destination = portal.getWormhole().getDestination();
				Vector direction = e.getLocation().getDirection();
				Vector velocity = e.getVelocity();
				Vector entry = VectorMath.directionNoNormal(portal.getPosition().getCenter(), e.getLocation());
				Direction closestDirection = Direction.closest(direction, portal.getIdentity().getFront(), portal.getIdentity().getBack());
				Direction closestVelocity = Direction.closest(velocity, portal.getIdentity().getFront(), portal.getIdentity().getBack());
				direction = closestDirection.equals(portal.getIdentity().getFront()) ? closestDirection.angle(direction, destination.getIdentity().getFront()) : closestDirection.angle(direction, destination.getIdentity().getBack());
				entry = closestDirection.equals(portal.getIdentity().getFront()) ? closestDirection.angle(entry, destination.getIdentity().getFront()) : closestDirection.angle(entry, destination.getIdentity().getBack());
				velocity = closestVelocity.equals(portal.getIdentity().getFront()) ? closestVelocity.angle(velocity, destination.getIdentity().getFront()) : closestVelocity.angle(velocity, destination.getIdentity().getBack());
				entry = portal.getIdentity().getFront().isVertical() ? new Vector(0, -1, 0) : entry;
				
				if(portal.getIdentity().getFront().isVertical() && !destination.getIdentity().getFront().isVertical())
				{
					direction = velocity.clone();
				}
				
				ArrivalVector vx = new ArrivalVector(velocity, direction, entry);
				sendArrival((RemotePortal) destination, e, vx);
			}
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
		
		if(TICK.tick % Settings.NETWORK_POPULATE_MAPPING_INTERVAL == 0 && Settings.ENABLE_PROJECTIONS)
		{
			for(Portal i : getPortals())
			{
				if(i instanceof RemotePortal && !i.getProjectionPlane().hasContent())
				{
					layer2StreamRequest(i);
					continue;
				}
			}
		}
		
		t.stop();
		TimingsService.root.get("mutex-handle").hit("mutex-service", t.getTime());
	}
	
	public void on(PlayerQuitEvent e)
	{
		actualVectors.remove(e.getPlayer());
	}
	
	public void dequeue(Portal p)
	{
		Wormholes.registry.destroyQueue.add(p);
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
		
		if(Wormholes.bus.isOnline())
		{
			String name = Wormholes.bus.getServerName();
			
			for(String i : Wormholes.bus.getServers())
			{
				if(!i.equals(name))
				{
					Transmission t = new Transmission(name, i, "id");
					GList<String> portals = new GList<String>();
					
					for(Portal j : getLocalPortals())
					{
						DataCluster data = j.toData();
						data.set("dn", j.getDisplayName());
						String c = data.toJSON().toString();
						portals.add(c);
					}
					
					t.set("p", portals);
					t.send();
				}
			}
			
			for(Transmission i : Wormholes.bus.getInbox())
			{
				if(i.getType().equals("id"))
				{
					Wormholes.bus.read(i);
					
					if(!Wormholes.registry.mutexPortals.containsKey(i.getSource()))
					{
						Wormholes.registry.mutexPortals.put(i.getSource(), new GList<Portal>());
					}
					
					Wormholes.registry.mutexPortals.get(i.getSource()).clear();
					
					for(String j : i.getStringList("p"))
					{
						DataCluster cc = new DataCluster(new JSONObject(j));
						RemotePortal rp = new RemotePortal(i.getSource(), null, cc.getString("dn"));
						rp.fromData(cc);
						Wormholes.registry.mutexPortals.get(i.getSource()).add(rp);
					}
				}
				
				else if(i.getType().equals("tp-r"))
				{
					UUID id = UUID.fromString(i.getString("id"));
					
					for(Player j : waiting.k())
					{
						if(j.getUniqueId().equals(id))
						{
							waiting.get(j).run();
							waiting.remove(j);
						}
					}
				}
				
				else if(i.getType().equals("a"))
				{
					UUID id = UUID.fromString(i.getString("id"));
					DataCluster rid = new DataCluster(new JSONObject(i.getString("rid")));
					ArrivalVector ar = new ArrivalVector(new Vector(), new Vector(), new Vector());
					ar.fromString(i.getString("v"));
					
					for(Portal j : getLocalPortals())
					{
						DataCluster a = j.toData();
						DataCluster b = rid.copy();
						a.remove("if");
						b.remove("if");
						
						if(a.toJSON().toString().equals(b.toJSON().toString()))
						{
							LocalPortal l = (LocalPortal) j;
							setArrival(l, id, ar);
						}
					}
				}
				
				else if(i.getType().equals("rld"))
				{
					Status.fdq = true;
					Wormholes.provider.getRasterer().dequeueAll();
					Wormholes.provider.getRasterer().flush();
					
					for(Entity j : EntityHologram.lock)
					{
						j.remove();
					}
					
					Bukkit.getPluginManager().disablePlugin(Wormholes.instance);
					Bukkit.getPluginManager().enablePlugin(Wormholes.instance);
				}
				
				else if(i.getType().equals("tp"))
				{
					Wormholes.bus.read(i);
					
					DataCluster cc = new DataCluster(new JSONObject(i.getString("to")));
					
					for(Portal j : Wormholes.registry.localPortals)
					{
						if(cc.equals(j.toData()))
						{
							LocalPortal target = (LocalPortal) j;
							UUID id = UUID.fromString(i.getString("id"));
							Vector velocity = new Vector(i.getDouble("vx"), i.getDouble("vy"), i.getDouble("vz"));
							Vector direction = new Vector(i.getDouble("dx"), i.getDouble("dy"), i.getDouble("dz"));
							Vector entry = new Vector(i.getDouble("ex"), i.getDouble("ey"), i.getDouble("ez"));
							setPending(id, target, velocity, direction, entry);
							
							if(Wormholes.bus.isOnline())
							{
								Transmission t = new Transmission(Wormholes.bus.getServerName(), i.getSource(), "tp-r");
								t.set("id", id.toString());
								t.send();
							}
							
							break;
						}
					}
				}
				
				else if(i.getType().equals("blk"))
				{
					Wormholes.bus.read(i);
					
					DataCluster cc = new DataCluster(new JSONObject(i.getString("id")));
					
					for(Portal j : getLocalPortals())
					{
						DataCluster a = cc.copy();
						DataCluster b = j.toData().copy();
						
						a.remove("if");
						b.remove("if");
						
						if(a.toJSON().toString().equals(b.toJSON().toString()))
						{
							LocalPortal target = (LocalPortal) j;
							Vector v = new Vector(i.getInt("vx"), i.getInt("vy"), i.getInt("vz"));
							@SuppressWarnings("deprecation")
							MaterialBlock mb = new MaterialBlock(Material.getMaterial(i.getInt("m")), i.getInt("d").byteValue());
							
							if(target.hasWormhole() && target.isWormholeMutex())
							{
								target.getWormhole().getDestination().getProjectionPlane().blockChange(v, mb);
							}
							
							break;
						}
					}
				}
				
				else if(i.getType().equals("mreq") && Settings.ENABLE_PROJECTIONS)
				{
					Wormholes.bus.read(i);
					
					DataCluster cc = new DataCluster(new JSONObject(i.getString("to")));
					
					for(Portal j : getLocalPortals())
					{
						DataCluster a = cc.copy();
						DataCluster b = j.toData().copy();
						
						a.remove("if");
						b.remove("if");
						
						if(a.toJSON().toString().equals(b.toJSON().toString()))
						{
							LocalPortal target = (LocalPortal) j;
							beginStream(target.getServer(), i.getSource(), target.toData().toJSON().toString(), target);
							break;
						}
					}
				}
				
				else if(i.getType().equals("sided"))
				{
					Wormholes.bus.read(i);
					DataCluster cc = new DataCluster(new JSONObject(i.getString("id")));
					
					for(Portal j : getLocalPortals())
					{
						DataCluster a = cc.copy();
						DataCluster b = j.toData().copy();
						
						a.remove("if");
						b.remove("if");
						
						if(a.toJSON().toString().equals(b.toJSON().toString()))
						{
							LocalPortal target = (LocalPortal) j;
							target.setSided(true);
							
							for(Player k : target.getPosition().getArea().getPlayers())
							{
								k.sendMessage("Received message to go SIDED");
							}
							
							break;
						}
					}
				}
				
				else if(i.getType().equals("usided"))
				{
					Wormholes.bus.read(i);
					DataCluster cc = new DataCluster(new JSONObject(i.getString("id")));
					
					for(Portal j : getLocalPortals())
					{
						DataCluster a = cc.copy();
						DataCluster b = j.toData().copy();
						
						a.remove("if");
						b.remove("if");
						
						if(a.toJSON().toString().equals(b.toJSON().toString()))
						{
							LocalPortal target = (LocalPortal) j;
							target.setSided(false);
							break;
						}
					}
				}
			}
		}
	}
	
	public void beginStream(String from, String to, String as, LocalPortal lp)
	{
		if(!Settings.ENABLE_PROJECTIONS)
		{
			return;
		}
		
		if(!lp.getProjectionPlane().hasContent())
		{
			lp.getProjectionPlane().sample(lp.getPosition().getCenter(), Settings.PROJECTION_SAMPLE_RADIUS, lp.getIdentity().getFront().isVertical());
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
									
									new ForwardedPluginMessage(Wormholes.instance, CL.L2.get(), to, boas).send();
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
		if(!Settings.ENABLE_PROJECTIONS)
		{
			return;
		}
		
		Transmission r = new Transmission(Wormholes.bus.getServerName(), remotePortalReference.getServer(), "mreq");
		r.set("to", remotePortalReference.toData().toJSON().toString());
		r.send();
	}
	
	public void layer2Stream(byte[] msgbytes)
	{
		if(!Settings.ENABLE_PROJECTIONS)
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
					
					for(String s : getMutexPortals().k())
					{
						for(Portal i : getMutexPortals().get(s))
						{
							
							DataCluster a = c.copy();
							DataCluster b = i.toData().copy();
							a.remove("if");
							b.remove("if");
							
							if(i instanceof RemotePortal && a.toJSON().toString().equals(b.toJSON().toString()))
							{
								i.getProjectionPlane().addSuperCompressed(data);
								break;
							}
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
	
	public void sendPlayerThrough(Player p, UUID id, LocalPortal source, Portal to, Vector velocity, Vector direction, Vector entry, Runnable r)
	{
		FinalInteger cd = new FinalInteger(Settings.NETWORK_PUSH_THRESHOLD / 50);
		
		new Task(0)
		{
			@Override
			public void run()
			{
				if(Wormholes.bus.isOnline())
				{
					waiting.put(p, r);
					String sn = Wormholes.bus.getServerName();
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
					t.send();
					cancel();
					return;
				}
				
				else
				{
					cd.sub(1);
				}
				
				if(cd.get() <= 0)
				{
					waiting.remove(p);
					cancel();
					Wormholes.fx.throwBack(p, Wormholes.fx.throwBackVector(p, source), source);
				}
			}
		};
		
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
		Wormholes.provider.movePlayer(e.getPlayer());
		addThrottle(e.getPlayer());
		
		for(LocalPortal i : arrivals.k())
		{
			for(UUID j : arrivals.get(i).k())
			{
				if(e.getPlayer().getUniqueId().equals(j))
				{
					ArrivalVector av = arrivals.get(i).get(j);
					Location position = i.getIdentity().getFront().isVertical() ? i.getPosition().getCenter() : i.getPosition().getCenterDown().clone().add(0, 1, 0);
					position.setDirection(av.getDirection());
					
					if(Settings.BUNGEECORD_SEND_ONLY)
					{
						continue;
					}
					
					new TaskLater()
					{
						@Override
						public void run()
						{
							e.getPlayer().teleport(position);
							e.getPlayer().setVelocity(av.getVelocity());
						}
					};
				}
			}
		}
	}
	
	public GList<Portal> getPortals()
	{
		GList<Portal> p = new GList<Portal>();
		p.add(Wormholes.registry.localPortals.copy());
		
		for(String i : Wormholes.registry.mutexPortals.k())
		{
			p.add(Wormholes.registry.mutexPortals.get(i).copy());
		}
		
		return p;
	}
	
	public GList<Portal> getLocalPortals()
	{
		return Wormholes.registry.localPortals.copy();
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
		return Wormholes.registry.mutexPortals;
	}
	
	public GMap<UUID, GQuadraset<Portal, Vector, Vector, Vector>> getPendingPulls()
	{
		return pendingPulls;
	}
	
	public void dequeueAll()
	{
		for(Portal i : getLocalPortals())
		{
			Wormholes.projector.deproject((LocalPortal) i);
		}
	}
	
	@EventHandler
	public void on(BlockPhysicsEvent e)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(e.getBlock().getLocation()))
			{
				blockChange(e.getBlock().getLocation(), i);
			}
		}
	}
	
	@EventHandler
	public void on(BlockFromToEvent e)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(e.getBlock().getLocation()))
			{
				blockChange(e.getBlock().getLocation(), i);
			}
		}
	}
	
	@EventHandler
	public void on(EntityExplodeEvent e)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(e.getLocation()))
			{
				for(Block j : e.blockList())
				{
					if(i.getPosition().getArea().contains(j.getLocation()))
					{
						blockChange(j.getLocation(), i);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void on(BlockBreakEvent e)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(e.getBlock().getLocation()))
			{
				if(i.getPosition().getPane().contains(e.getBlock().getLocation()))
				{
					if(i.getPosition().getCenterDown().equals(e.getBlock().getLocation()) || i.getPosition().getCenterUp().equals(e.getBlock().getLocation()) || i.getPosition().getCenterLeft().equals(e.getBlock().getLocation()) || i.getPosition().getCenterRight().equals(e.getBlock().getLocation()))
					{
						if(!canDestroy(e.getPlayer()))
						{
							e.setCancelled(true);
							new GSound(MSound.BLAZE_HIT.bukkitSound(), 1f, 1.5f + (float) (Math.random() * 0.2)).play(e.getBlock().getLocation());
							Wormholes.fx.phaseDeny((LocalPortal) i, e.getBlock().getLocation().add(0.5, 0.5, 0.5));
						}
					}
					
					else if(!canBuild(e.getPlayer()))
					{
						e.setCancelled(true);
						new GSound(MSound.BLAZE_HIT.bukkitSound(), 1f, 1.5f + (float) (Math.random() * 0.2)).play(e.getBlock().getLocation());
						Wormholes.fx.phaseDeny((LocalPortal) i, e.getBlock().getLocation().add(0.5, 0.5, 0.5));
					}
				}
				
				blockChange(e.getBlock().getLocation(), i);
			}
		}
	}
	
	public void blockChange(Location lx, Portal p)
	{
		new TaskLater()
		{
			@Override
			public void run()
			{
				for(Block i : W.blockFaces(lx.getBlock()))
				{
					Location l = i.getLocation();
					
					if(p.hasWormhole())
					{
						Vector v = VectorMath.directionNoNormal(p.getPosition().getCenter(), l.getBlock().getLocation()).clone().add(new Vector(0.5, 0.5, 0.5));
						v = p.getIdentity().getFront().angle(v, p.getIdentity().getFront());
						
						MaterialBlock m = new MaterialBlock(l);
						p.getProjectionPlane().blockChange(v, m);
						
						if(p.isWormholeMutex())
						{
							sendBlockChange(v, m, p.getWormhole().getDestination());
						}
						
						else
						{
							for(Player j : p.getWormhole().getDestination().getPosition().getArea().getPlayers())
							{
								Wormholes.provider.movePlayer(j);
								((LocalPortal) p.getWormhole().getDestination()).getMask().sched(j);
							}
						}
					}
				}
			}
		};
	}
	
	@SuppressWarnings("deprecation")
	public void sendBlockChange(Vector v, MaterialBlock m, Portal destination)
	{
		Transmission t = new Transmission(Wormholes.bus.getServerName(), destination.getServer(), "blk");
		t.set("id", destination.toData().toJSON().toString());
		t.set("vx", v.getBlockX());
		t.set("vy", v.getBlockY());
		t.set("vz", v.getBlockZ());
		t.set("m", m.getMaterial().getId());
		t.set("d", m.getData().intValue());
		t.send();
	}
	
	@EventHandler
	public void on(BlockPlaceEvent e)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(e.getBlock().getLocation()))
			{
				if(i.getPosition().getPane().contains(e.getBlock().getLocation()))
				{
					if(!canBuild(e.getPlayer()))
					{
						e.setCancelled(true);
						new GSound(MSound.BLAZE_HIT.bukkitSound(), 1f, 1.5f + (float) (Math.random() * 0.2)).play(e.getBlock().getLocation());
						Wormholes.fx.phaseDeny((LocalPortal) i, e.getBlock().getLocation().add(0.5, 0.5, 0.5));
					}
				}
				
				blockChange(e.getBlock().getLocation(), i);
			}
		}
	}
	
	public boolean canBuild(Player p)
	{
		return new Permissable(p).canBuild();
	}
	
	public boolean canDestroy(Player p)
	{
		return new Permissable(p).canDestroy();
	}
	
	public void updateEverything(Runnable inject)
	{
		Wormholes.provider.dfs();
		inject.run();
		Wormholes.provider.dfd();
		Wormholes.instance.doReload();
	}
	
	public void globalReload()
	{
		Transmission t = new Transmission(Wormholes.bus.getServerName(), "ALL", "rld");
		t.forceSend();
	}
	
	public GMap<Player, Runnable> getWaiting()
	{
		return waiting;
	}
	
	public GMap<Player, Vector> getActualVectors()
	{
		return actualVectors;
	}
	
	public GMap<LocalPortal, GMap<UUID, ArrivalVector>> getArrivals()
	{
		return arrivals;
	}
	
	public GList<Integer> getiLock()
	{
		return iLock;
	}
	
	public void clearVectorCache()
	{
		actualVectors.clear();
	}
	
	public Vector getActualVector(Player p)
	{
		return actualVectors.containsKey(p) ? actualVectors.get(p) : p.getVelocity();
	}
	
	public EntityHider getHider()
	{
		return hider;
	}
}
