package com.volmit.wormholes.service;

import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Status;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.portal.Wormhole;
import com.volmit.wormholes.projection.NulledViewport;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.projection.RenderMesh;
import com.volmit.wormholes.projection.Viewport;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.Execution;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.Timer;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.Wraith;

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
		if(!projecting && Settings.ENABLE_PROJECTIONS)
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
							
							for(Portal po : Wormholes.host.getLocalPortals())
							{
								if(((LocalPortal) po).getSided())
								{
									continue;
								}
								
								try
								{
									if(lastPort.containsKey(po))
									{
										for(Player i : lastPort.get(po).k())
										{
											if(!po.getPosition().getArea().contains(i.getLocation()))
											{
												lastPort.get(po).remove(i);
												deproject((LocalPortal) po, i);
											}
										}
									}
									
									for(Entity j : po.getPosition().getBoundingBox().getExiting())
									{
										if(j instanceof Player)
										{
											deproject((LocalPortal) po);
										}
									}
									
									if(po.getPosition().getArea().hasPlayers() && ((LocalPortal) po).getSettings().isProject() && ((LocalPortal) po).getMask().needsProjection())
									{
										project((LocalPortal) po);
										Wormholes.pool.lock();
										((LocalPortal) po).getMask().clear();
										
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
	
	public void deproject(LocalPortal p, Player i)
	{
		Iterator<Block> it = p.getPosition().getArea().iterator();
		
		while(it.hasNext())
		{
			Block b = it.next();
			Wormholes.provider.getRasterer().dequeue(i, b.getLocation());
		}
		
		Wormholes.provider.getRasterer().get(i).flush();
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
			Wormhole w = p.getWormhole();
			PortalIdentity identity = w.getDestination().getIdentity();
			ProjectionPlane plane = w.getDestination().getProjectionPlane();
			
			if(plane.hasContent())
			{
				GMap<Player, Viewport> view = Wormholes.provider.getViewport(p);
				GMap<Vector, MaterialBlock> map = plane.remap(identity.getFront(), p.getIdentity().getFront());
				
				if(map.isEmpty() || map.size() < 250)
				{
					if(!p.isWormholeMutex())
					{
						plane.getMapping().clear();
						plane.getRemapCache().clear();
						plane.getOrmapCache().clear();
					}
					
					return;
				}
				
				if(view.isEmpty())
				{
					return;
				}
				
				for(Player i : view.k())
				{
					Viewport vIn = view.get(i);
					Viewport vOut = lastPort.containsKey(p) && lastPort.get(p).containsKey(i) ? lastPort.get(p).get(i) : new NulledViewport(i, p);
					boolean br = false;
					
					Wormholes.pool.queue(new Execution()
					{
						@Override
						public void run()
						{
							for(Iterator<Block> it : vIn.getProjectionSet().iterator())
							{
								while(it.hasNext())
								{
									Block j = it.next();
									
									if(vIn.contains(j))
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
							}
						}
					});
					
					Wormholes.pool.queue(new Execution()
					{
						@Override
						public void run()
						{
							for(Iterator<Block> it : vOut.getProjectionSet().iterator())
							{
								while(it.hasNext())
								{
									Block j = it.next();
									
									if(vOut.getProjectionSet().contains(j) && !vIn.contains(j))
									{
										Wormholes.provider.getRasterer().dequeue(i, j.getLocation());
									}
									
									Status.permutationsPerSecond++;
								}
							}
						}
					});
					
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
