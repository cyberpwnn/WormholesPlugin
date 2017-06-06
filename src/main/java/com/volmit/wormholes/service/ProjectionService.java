package com.volmit.wormholes.service;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Status;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.projection.NulledViewport;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.projection.RenderMesh;
import com.volmit.wormholes.projection.Viewport;
import com.volmit.wormholes.wormhole.Wormhole;
import wraith.A;
import wraith.Axis;
import wraith.Cuboid;
import wraith.GList;
import wraith.GMap;
import wraith.M;
import wraith.MaterialBlock;
import wraith.TICK;
import wraith.Timer;
import wraith.VectorMath;
import wraith.Wraith;

public class ProjectionService implements Listener
{
	private GMap<PortalKey, ProjectionPlane> remotePlanes;
	private Boolean projecting;
	private Long tpl;
	private GMap<Portal, GMap<Player, Viewport>> lastPort;
	private RenderMesh mesh;
	private long lms;
	
	public ProjectionService()
	{
		remotePlanes = new GMap<PortalKey, ProjectionPlane>();
		projecting = false;
		tpl = M.ms();
		lastPort = new GMap<Portal, GMap<Player, Viewport>>();
		mesh = new RenderMesh();
		Wraith.registerListener(this);
		lms = M.ms();
	}
	
	public void flush()
	{
		if(!projecting && Settings.ENABLE_PROJECTIONS && TICK.tick % Settings.PROJECTION_MAX_SPEED == 0)
		{
			projecting = true;
			
			try
			{
				new A()
				{
					@Override
					public void async()
					{
						try
						{
							Timer t = new Timer();
							t.start();
							
							for(Portal i : Wormholes.host.getLocalPortals())
							{
								try
								{
									if(i.getPosition().getArea().hasPlayers() && ((LocalPortal) i).getSettings().isProject())
									{
										project((LocalPortal) i);
										
										try
										{
											if(M.ms() - lms > Settings.NETWORK_FLUSH_THRESHOLD)
											{
												lms = M.ms();
												Wormholes.provider.getRasterer().flush();
											}
										}
										
										catch(Exception e)
										{
											
										}
									}
								}
								
								catch(Exception e)
								{
									
								}
							}
							
							projecting = false;
							
							t.stop();
							TimingsService.asyn.get("mutex-handle").hit("projection-service", t.getTime());
							Status.projectionTime = (double) t.getTime() / 1000000.0;
						}
						
						catch(IllegalStateException e)
						{
							projecting = false;
						}
					}
				};
			}
			
			catch(IllegalStateException e)
			{
				projecting = false;
			}
		}
	}
	
	public void deproject(LocalPortal p)
	{
		if(lastPort.containsKey(p))
		{
			for(Player i : lastPort.get(p).k())
			{
				Viewport v = lastPort.get(p).get(i);
				v.wipe();
			}
			
			lastPort.remove(p);
			Wormholes.provider.getRasterer().flush();
		}
	}
	
	public void project(LocalPortal p)
	{
		if(Wormholes.registry.destroyQueue.contains(p))
		{
			for(Entity k : p.getPosition().getBoundingBox().getInside())
			{
				if(k instanceof Player)
				{
					Wormholes.provider.getRasterer().get((Player) k).dequeueAll();
					Wormholes.provider.movePlayer((Player) k);
				}
			}
			
			Wormholes.registry.destroyQueue.remove(p);
			return;
		}
		
		if(p.hasWormhole())
		{
			((LocalPortal) p).setLoading(false);
			Wormhole w = p.getWormhole();
			PortalIdentity identity = w.getDestination().getIdentity();
			ProjectionPlane plane = w.getDestination().getProjectionPlane();
			
			if(plane.hasContent())
			{
				GMap<Player, Viewport> view = Wormholes.provider.getViewport(p);
				GMap<Vector, MaterialBlock> map = plane.remap(identity.getFront(), p.getIdentity().getFront());
				
				if(view.isEmpty())
				{
					return;
				}
				
				for(Player i : view.k())
				{
					Viewport vIn = view.get(i);
					Viewport vOut = lastPort.containsKey(p) && lastPort.get(p).containsKey(i) ? lastPort.get(p).get(i) : new NulledViewport(i, p);
					boolean br = false;
					
					for(Block j : vIn.getProjectionSet().getBlocks())
					{
						if(vIn.contains(j.getLocation()))
						{
							Vector dir = VectorMath.directionNoNormal(p.getPosition().getCenter(), j.getLocation());
							Vector vec = dir.clone().add(new Vector(0.5, 0.5, 0.5));
							p.getIdentity().getFront().angle(vec, identity.getFront());
							MaterialBlock mb = map.get(vec);
							
							if(mb == null)
							{
								continue;
							}
							
							Wormholes.provider.getRasterer().queue(i, j.getLocation(), mb);
						}
						
						Status.permutationsPerSecond++;
					}
					
					for(Block j : vOut.getProjectionSet().getBlocks())
					{
						if(vOut.contains(j.getLocation()) && !vIn.contains(j.getLocation()))
						{
							Wormholes.provider.getRasterer().dequeue(i, j.getLocation());
						}
						
						Status.permutationsPerSecond++;
					}
					
					if(br)
					{
						continue;
					}
					
					if(!lastPort.containsKey(p))
					{
						lastPort.put(p, new GMap<Player, Viewport>());
					}
					
					lastPort.get(p).put(i, view.get(i));
				}
			}
		}
	}
	
	@EventHandler
	public void on(PlayerTeleportEvent e)
	{
		if(M.ms() - tpl > 1000)
		{
			if(e.getCause().equals(TeleportCause.UNKNOWN))
			{
				tpl = M.ms();
				Wormholes.provider.getRasterer().dequeue(e.getPlayer(), e.getPlayer().getLocation().getBlock().getLocation());
				
				Cuboid c = new Cuboid(e.getTo());
				c = c.e(Axis.X, 16).e(Axis.Y, 8).e(Axis.Z, 16);
				
				for(Block i : new GList<Block>(c.iterator()))
				{
					Wormholes.provider.getRasterer().dequeue(e.getPlayer(), i.getLocation());
				}
				
				e.setTo(e.getTo().clone().add(0, 0.3, 0));
			}
		}
	}
	
	public GMap<PortalKey, ProjectionPlane> getRemotePlanes()
	{
		return remotePlanes;
	}
	
	public Boolean getProjecting()
	{
		return projecting;
	}
	
	public Long getTpl()
	{
		return tpl;
	}
	
	public GMap<Portal, GMap<Player, Viewport>> getLastPort()
	{
		return lastPort;
	}
	
	public RenderMesh getMesh()
	{
		return mesh;
	}
}
