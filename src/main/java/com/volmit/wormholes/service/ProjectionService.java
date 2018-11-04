package com.volmit.wormholes.service;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.volume.bukkit.task.A;
import com.volmit.volume.bukkit.task.S;
import com.volmit.volume.math.M;
import com.volmit.volume.math.Profiler;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.projection.Viewport;
import com.volmit.wormholes.renderer.RenderTask;
import com.volmit.wormholes.renderer.RenderTaskMode;
import com.volmit.wormholes.renderer.ViewportLatch;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;

public class ProjectionService implements Listener
{
	private GMap<PortalKey, ProjectionPlane> remotePlanes;
	private GMap<LocalPortal, GMap<Player, ViewportLatch>> viewports;
	private Boolean projecting;
	private GList<RenderTask> renderTasks;
	private long last = M.ms();
	public static double renderTime;
	public static double renderProgress;
	public static double renderersActive;
	public static double renderersTotal;

	public ProjectionService()
	{
		renderTime = 0;
		DB.d(this, "Starting Projection Service");
		projecting = false;
		viewports = new GMap<LocalPortal, GMap<Player, ViewportLatch>>();
		remotePlanes = new GMap<PortalKey, ProjectionPlane>();
		renderTasks = new GList<RenderTask>();
	}


	public void flush()
	{
		if(!projecting && Settings.ENABLE_PROJECTIONS)
		{
			projecting = true;

			new A()
			{
				@Override
				public void run()
				{
					try
					{
						processViewports();
						queueProjections();
						renderTime = project(1500D);
						projecting = false;

						if(M.ms() - last > 50)
						{
							Wormholes.provider.getRasterer().flush();
							last = M.ms();
						}
					}

					catch(Throwable e)
					{
						e.printStackTrace();
					}
				}
			};
		}
	}

	private void processViewports()
	{
		removeLatches();

		for(Portal portal : Wormholes.host.getLocalPortals())
		{
			for(Player player : ((LocalPortal) portal).getPlayers())
			{
				getLatch((LocalPortal) portal, player).update();
			}
		}
	}

	private void queueProjections()
	{
		for(RenderTask task : renderTasks.copy())
		{
			if(task.isDone() && !task.getMode().equals(RenderTaskMode.QUEUE))
			{
				renderTasks.remove(task);
			}
		}

		for(Portal portal : Wormholes.host.getLocalPortals())
		{
			for(Player player : ((LocalPortal) portal).getPlayers())
			{
				try
				{
					boolean queue = true;

					for(RenderTask task : renderTasks.copy())
					{
						if(task.getPortal().equals(portal) && task.getPlayer().equals(player))
						{
							if(task.getMode().equals(RenderTaskMode.DEQUEUE_ALL) && !task.isDone())
							{
								queue = false;
								continue;
							}

							if(task.getMode().equals(RenderTaskMode.QUEUE))
							{
								if(getViewport((LocalPortal) portal, player).equals(task.getViewport()))
								{
									queue = false;
									continue;
								}

								if(task.isDone())
								{
									renderTasks.remove(task);
								}
							}
						}
					}

					if((queue || ((LocalPortal) portal).getMask().needsProjection()) && !portal.getProjectionPlane().isBusy())
					{
						if(((LocalPortal) portal).getMask().needsProjection())
						{
							renderTasks.add(new RenderTask(player, getViewport((LocalPortal) portal, player), getLastViewport((LocalPortal) portal, player), (LocalPortal) portal, RenderTaskMode.QUEUE_ALL));
						}

						else
						{
							try
							{
								renderTasks.add(new RenderTask(player, getViewport((LocalPortal) portal, player), getLastViewport((LocalPortal) portal, player), (LocalPortal) portal, RenderTaskMode.QUEUE));
							}

							catch(Throwable e)
							{

							}
						}
					}
				}

				catch(Exception e)
				{

				}
			}

			((LocalPortal) portal).getMask().clear();
		}
	}

	private double project(double totalMs)
	{
		int activeTasks = 0;
		double pg = 0;
		Profiler pr = new Profiler();
		pr.begin();

		for(RenderTask i : renderTasks)
		{
			if(i.isDone())
			{
				continue;
			}

			activeTasks++;
			pg += i.getRenderlet().getProgress();
		}

		if(activeTasks > 0)
		{
			double maxAllocPer = totalMs / (double) activeTasks;

			for(RenderTask i : renderTasks.copy())
			{
				if(i.isDone())
				{
					continue;
				}

				try
				{
					i.render(maxAllocPer);
				}

				catch(Throwable e)
				{

				}
			}
		}

		renderersTotal = renderTasks.size();
		renderProgress = pg;
		renderersActive = activeTasks;

		pr.end();
		return pr.getMilliseconds();
	}

	public GMap<PortalKey, ProjectionPlane> getRemotePlanes()
	{
		return remotePlanes;
	}

	public void deproject(LocalPortal l)
	{
		for(Player i : l.getPlayers())
		{
			deproject(l, i);
		}
	}

	public void deproject(LocalPortal l, Player i)
	{
		try
		{
			for(RenderTask j : renderTasks.copy())
			{
				if(j == null)
				{
					renderTasks.remove(j);
					continue;
				}

				if(j.getPlayer() == null || j.getPortal() == null)
				{
					renderTasks.remove(j);
					continue;
				}

				if(j.getPlayer().equals(i) && j.getPortal().equals(l))
				{
					renderTasks.remove(j);
				}
			}

			renderTasks.add(new RenderTask(i, l, RenderTaskMode.DEQUEUE_ALL));
		}

		catch(Exception e)
		{

		}
	}

	public void removeLatches()
	{
		GList<Portal> portals = Wormholes.host.getLocalPortals();

		for(LocalPortal i : viewports.k())
		{
			if(!portals.contains(i))
			{
				viewports.remove(i);
				continue;
			}

			new S()
			{
				@Override
				public void run()
				{
					GList<Player> players = i.getPlayers();

					new A()
					{
						@Override
						public void run()
						{
							for(Player j : viewports.get(i).k())
							{
								if(!players.contains(j))
								{
									viewports.get(i).remove(j);
									deproject(i, j);
								}
							}
						}
					};
				}
			};
		}
	}

	private Viewport getViewport(LocalPortal portal, Player player)
	{
		return getLatch(portal, player).getCurrentViewport();
	}

	private Viewport getLastViewport(LocalPortal portal, Player player)
	{
		return getLatch(portal, player).getLastViewport();
	}

	private ViewportLatch getLatch(LocalPortal portal, Player player)
	{
		if(!viewports.containsKey(portal))
		{
			viewports.put(portal, new GMap<Player, ViewportLatch>());
		}

		if(!viewports.get(portal).containsKey(player))
		{
			viewports.get(portal).put(player, new ViewportLatch(player, portal));
		}

		return viewports.get(portal).get(player);
	}

	public GMap<LocalPortal, GMap<Player, ViewportLatch>> getViewports()
	{
		return viewports;
	}
}
