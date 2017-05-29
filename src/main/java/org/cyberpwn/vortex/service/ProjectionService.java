package org.cyberpwn.vortex.service;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.Status;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.portal.PortalIdentity;
import org.cyberpwn.vortex.portal.PortalKey;
import org.cyberpwn.vortex.projection.NulledViewport;
import org.cyberpwn.vortex.projection.ProjectionPlane;
import org.cyberpwn.vortex.projection.RenderMesh;
import org.cyberpwn.vortex.projection.Viewport;
import org.cyberpwn.vortex.wormhole.Wormhole;
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
	
	public ProjectionService()
	{
		remotePlanes = new GMap<PortalKey, ProjectionPlane>();
		projecting = false;
		tpl = M.ms();
		lastPort = new GMap<Portal, GMap<Player, Viewport>>();
		mesh = new RenderMesh();
		Wraith.registerListener(this);
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
							
							for(Portal i : VP.host.getLocalPortals())
							{
								try
								{
									if(i.getPosition().getArea().hasPlayers() && ((LocalPortal) i).getSettings().isProject())
									{
										project((LocalPortal) i);
									}
								}
								
								catch(Exception e)
								{
									
								}
							}
							
							VP.provider.getRasterer().flush();
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
		}
	}
	
	public void project(LocalPortal p)
	{
		if(VP.registry.destroyQueue.contains(p))
		{
			for(Entity k : p.getPosition().getBoundingBox().getInside())
			{
				if(k instanceof Player)
				{
					VP.provider.getRasterer().get((Player) k).dequeueAll();
					VP.provider.movePlayer((Player) k);
				}
			}
			
			VP.registry.destroyQueue.remove(p);
			return;
		}
		
		if(p.hasWormhole())
		{
			Wormhole w = p.getWormhole();
			PortalIdentity identity = w.getDestination().getIdentity();
			ProjectionPlane plane = w.getDestination().getProjectionPlane();
			
			if(plane.hasContent())
			{
				GMap<Player, Viewport> view = VP.provider.getViewport(p);
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
							
							VP.provider.getRasterer().queue(i, j.getLocation(), mb);
						}
					}
					
					for(Block j : vOut.getProjectionSet().getBlocks())
					{
						if(vOut.contains(j.getLocation()) && !vIn.contains(j.getLocation()))
						{
							VP.provider.getRasterer().dequeue(i, j.getLocation());
						}
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
				VP.provider.getRasterer().dequeue(e.getPlayer(), e.getPlayer().getLocation().getBlock().getLocation());
				
				Cuboid c = new Cuboid(e.getTo());
				c = c.e(Axis.X, 16).e(Axis.Y, 8).e(Axis.Z, 16);
				
				for(Block i : new GList<Block>(c.iterator()))
				{
					VP.provider.getRasterer().dequeue(e.getPlayer(), i.getLocation());
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
