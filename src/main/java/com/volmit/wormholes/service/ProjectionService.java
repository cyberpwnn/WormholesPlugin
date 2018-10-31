package com.volmit.wormholes.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.volume.bukkit.task.A;
import com.volmit.volume.lang.format.F;
import com.volmit.volume.math.M;
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
import com.volmit.wormholes.util.NMSX;

public class ProjectionService implements Listener
{
	private GMap<PortalKey, ProjectionPlane> remotePlanes;
	private GMap<LocalPortal, GMap<Player, ViewportLatch>> viewports;
	private Boolean projecting;
	private GList<RenderTask> renderTasks;
	private long last = M.ms();

	public ProjectionService()
	{
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
						project(2000D);
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

				if(queue && !portal.getProjectionPlane().isBusy())
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
	}

	private void project(double totalMs)
	{
		int activeTasks = 0;
		double pg = 0;

		for(RenderTask i : renderTasks)
		{
			if(i.isDone())
			{
				continue;
			}

			activeTasks++;
			pg += i.getRenderlet().getProgress();
		}

		double maxAllocPer = totalMs / (double) activeTasks;

		for(Player i : Bukkit.getOnlinePlayers())
		{
			NMSX.sendActionBar(i, "Active: " + renderTasks.size() + " Progress: " + F.pc(pg / (double) activeTasks));
		}

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
		for(RenderTask j : renderTasks.copy())
		{
			if(j.getPlayer().equals(i) && j.getPortal().equals(l))
			{
				renderTasks.remove(j);
			}
		}

		renderTasks.add(new RenderTask(i, l, RenderTaskMode.DEQUEUE_ALL));
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

			GList<Player> players = i.getPlayers();

			for(Player j : viewports.get(i).k())
			{
				if(!players.contains(j))
				{
					viewports.get(i).remove(j);
					deproject(i, j);
				}
			}
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
}
