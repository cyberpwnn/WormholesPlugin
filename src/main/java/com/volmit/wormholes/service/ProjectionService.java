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
import com.volmit.wormholes.projection.RenderMode;
import com.volmit.wormholes.projection.RenderStage;
import com.volmit.wormholes.projection.VRBuilder;
import com.volmit.wormholes.projection.VRM;
import com.volmit.wormholes.projection.VRMLock;
import com.volmit.wormholes.projection.Viewport;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.Execution;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.TaskLater;
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
	private VRMLock lock;

	public ProjectionService()
	{
		DB.d(this, "Starting Projection Service");
		remotePlanes = new GMap<PortalKey, ProjectionPlane>();
		projecting = false;
		tpl = M.ms();
		lastPort = new GMap<Portal, GMap<Player, Viewport>>();
		mesh = new RenderMesh();
		Wraith.registerListener(this);
		lms = M.ms();
		lock = new VRMLock();
	}

	public void flush()
	{
		if(!projecting && Settings.ENABLE_PROJECTIONS)
		{
			projecting = true;
			flushProjections();
		}
	}

	private void flushProjections()
	{
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

						new TaskLater()
						{
							@Override
							public void run()
							{
								for(Portal po : Wormholes.host.getLocalPortals())
								{
									if(((LocalPortal) po).getSided())
									{
										continue;
									}

									try
									{
										new A()
										{

											@Override
											public void async()
											{
												deprojectStrays(po);
											}
										};
										startProjection(po);
									}

									catch(Exception e)
									{

									}
								}

								new A()
								{
									@Override
									public void async()
									{
										projecting = false;
										postProject(t);
									}
								};
							}
						};
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

	private void postProject(Timer t)
	{
		t.stop();
		TimingsService.asyn.get("mutex-handle").hit("projection-service", t.getTime());
		Status.projectionTime = t.getTime() / 1000000.0;
	}

	private void startProjection(Portal po)
	{
		if(po.getPosition().getArea().hasPlayers() && ((LocalPortal) po).getSettings().isProject() && ((LocalPortal) po).getMask().needsProjection())
		{
			new A()
			{
				@Override
				public void async()
				{
					project((LocalPortal) po);
					Wormholes.pool.lock();
					((LocalPortal) po).getMask().clear();
					doProject();
				}
			};
		}
	}

	private void doProject()
	{
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

	private void deprojectStrays(Portal po)
	{
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
		}

		catch(Exception e)
		{

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
		preDeconstruct(p);

		if(p.hasWormhole())
		{
			Wormhole w = p.getWormhole();
			PortalIdentity identity = w.getDestination().getIdentity();
			ProjectionPlane plane = w.getDestination().getProjectionPlane();

			if(plane.hasContent())
			{
				try
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

						if(Settings.USE_OLD_RENDER_METHOD)
						{
							queueViewIn(vIn, p, identity, map, i);
							queueViewOut(vOut, vIn, i);
						}

						else
						{
							renderStage(i, p, map, vIn, vOut);
						}

						clearVRBuffers(p, i, view);
					}
				}

				catch(Exception e)
				{

				}
			}
		}
	}

	private VRM createVRM(Portal portal, Player player, Viewport dialate, Viewport erode, GMap<Vector, MaterialBlock> dimension)
	{
		VRBuilder builder = new VRBuilder(portal, player);
		builder.setDimension(dimension);
		builder.setStage(new RenderStage(Settings.PROJECTION_SAMPLE_RADIUS));
		builder.setMode(RenderMode.DIALATE);
		builder.setView(dialate);
		VRM vrm = new VRM(builder, dialate, erode);
		return vrm;
	}

	private void renderStage(Player player, Portal portal, GMap<Vector, MaterialBlock> dimension, Viewport dialate, Viewport erode)
	{
		if(!lock.hasVRM(portal, player) || (!lock.getVRM(portal, player).getDialater().equals(dialate) || !lock.getVRM(portal, player).getEroder().equals(erode) || lock.getVRM(portal, player).isComplete()))
		{
			VRM vrm = createVRM(portal, player, dialate, erode, dimension);
			lock.putVRM(portal, player, vrm);
		}

		lock.getVRM(portal, player).renderAll();
	}

	private void clearVRBuffers(LocalPortal p, Player i, GMap<Player, Viewport> view)
	{
		if(!lastPort.containsKey(p))
		{
			lastPort.put(p, new GMap<Player, Viewport>());
		}

		lastPort.get(p).put(i, view.get(i));
	}

	private void preDeconstruct(Portal p)
	{
		if(Wormholes.registry.destroyQueue.contains(p))
		{
			for(Entity k : p.getPosition().getBoundingBox().getInside())
			{
				if(k instanceof Player)
				{
					try
					{
						Wormholes.provider.getRasterer().get((Player) k).dequeueAll();
						Wormholes.provider.movePlayer((Player) k);
					}

					catch(Exception e)
					{

					}
				}
			}

			Wormholes.registry.destroyQueue.remove(p);
			return;
		}
	}

	public void queueViewOut(Viewport vOut, Viewport vIn, Player i)
	{
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
	}

	public void queueViewIn(Viewport vIn, Portal p, PortalIdentity identity, GMap<Vector, MaterialBlock> map, Player i)
	{
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
