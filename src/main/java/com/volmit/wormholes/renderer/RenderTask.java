package com.volmit.wormholes.renderer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.volume.lang.collections.Callback;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.projection.NulledViewport;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.projection.Viewport;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.VectorMath;

public class RenderTask
{
	public static double msPerIteration = 0.01;
	private Renderlet renderlet;
	private Viewport viewport;
	private Player player;
	private LocalPortal portal;
	private ProjectionPlane plane;
	private GMap<Vector, MaterialBlock> mapping;
	private RenderTaskMode mode;
	private boolean hasStarted;
	private Viewport oldPort;

	public RenderTask(Player player, LocalPortal portal, RenderTaskMode mode)
	{
		this(player, new NulledViewport(player, portal), new NulledViewport(player, portal), portal, mode);
	}

	public RenderTask(Player player, Viewport viewport, LocalPortal portal, RenderTaskMode mode)
	{
		this(player, viewport, viewport, portal, mode);
	}

	public RenderTask(Player player, Viewport viewport, Viewport oldPort, LocalPortal portal, RenderTaskMode mode)
	{
		this.player = player;
		this.viewport = viewport;
		this.portal = portal;
		this.oldPort = oldPort;
		this.mode = mode;
		hasStarted = false;

		if(oldPort != null && !(oldPort instanceof NulledViewport))
		{
			oldPort = oldPort.extend(10);
		}

		if(mode.equals(RenderTaskMode.DEQUEUE_ALL))
		{
			renderlet = new Renderlet(portal.getPosition().getArea(), Direction.D, portal);
		}

		else if(mode.equals(RenderTaskMode.QUEUE))
		{
			renderlet = new Renderlet(viewport.getCuboid().getBoundingCuboid(oldPort.getCuboid()), viewport.getDirection(), portal);
			plane = portal.getWormhole().getDestination().getProjectionPlane();
			mapping = portal.getWormhole().getDestination().getProjectionPlane().remap(portal.getIdentity().getFront(), portal.getWormhole().getDestination().getIdentity().getFront());
		}

		else if(mode.equals(RenderTaskMode.QUEUE_ALL))
		{
			renderlet = new Renderlet(viewport.getCuboid().getBoundingCuboid(oldPort.getCuboid()), viewport.getDirection(), portal);
			plane = portal.getWormhole().getDestination().getProjectionPlane();
			mapping = portal.getWormhole().getDestination().getProjectionPlane().remap(portal.getIdentity().getFront(), portal.getWormhole().getDestination().getIdentity().getFront());
		}
	}

	public boolean isDone()
	{
		if(renderlet == null)
		{
			return true;
		}

		return renderlet.isDone();
	}

	public void render(double maxMs)
	{
		if(isDone())
		{
			return;
		}

		if(mode.equals(RenderTaskMode.QUEUE))
		{
			renderQueue(maxMs);
		}

		if(mode.equals(RenderTaskMode.QUEUE_ALL))
		{
			renderQueueAll(maxMs);
		}

		if(mode.equals(RenderTaskMode.DEQUEUE_ALL))
		{
			renderDequeueAll(maxMs);
		}

		if(!hasStarted)
		{
			hasStarted = true;
		}
	}

	public static int getMaxIterationsForMS(double maxMs)
	{
		try
		{
			return (int) (maxMs / msPerIteration);
		}

		catch(Throwable e)
		{
			return 900;
		}
	}

	private void renderQueue(double maxMs)
	{
		renderlet.render(getMaxIterationsForMS(maxMs), new Callback<Location>()
		{
			@Override
			public void run(Location l)
			{
				if(plane.hasContent() && viewport.contains(l) && ((oldPort != null && !viewport.equals(oldPort)) ? !oldPort.contains(l) : true))
				{
					Vector dir = VectorMath.directionNoNormal(portal.getPosition().getCenter(), l);
					Vector vec = dir.clone().add(new Vector(0.5, 0.5, 0.5));
					portal.getIdentity().getFront().angle(vec, portal.getWormhole().getDestination().getIdentity().getFront());
					MaterialBlock mb = mapping.get(vec);

					if(mb == null)
					{
						return;
					}

					Wormholes.provider.getRasterer().queue(player, l, mb);
				}

				if(oldPort != null && !viewport.equals(oldPort) && plane.hasContent() && oldPort.contains(l) && !viewport.contains(l))
				{
					Wormholes.provider.getRasterer().dequeue(player, l);
				}
			}
		});
	}

	private void renderQueueAll(double maxMs)
	{
		renderlet.render(getMaxIterationsForMS(maxMs), new Callback<Location>()
		{
			@Override
			public void run(Location l)
			{
				if(plane.hasContent() && viewport.contains(l))
				{
					Vector dir = VectorMath.directionNoNormal(portal.getPosition().getCenter(), l);
					Vector vec = dir.clone().add(new Vector(0.5, 0.5, 0.5));
					portal.getIdentity().getFront().angle(vec, portal.getWormhole().getDestination().getIdentity().getFront());
					MaterialBlock mb = mapping.get(vec);

					if(mb == null)
					{
						return;
					}

					Wormholes.provider.getRasterer().queue(player, l, mb);
				}
			}
		});
	}

	private void renderDequeueAll(double maxMs)
	{
		renderlet.render(getMaxIterationsForMS(maxMs), new Callback<Location>()
		{
			@Override
			public void run(Location l)
			{
				Wormholes.provider.getRasterer().dequeue(player, l);
			}
		});
	}

	public static double getMsPerIteration()
	{
		return msPerIteration;
	}

	public Renderlet getRenderlet()
	{
		return renderlet;
	}

	public Viewport getViewport()
	{
		return viewport;
	}

	public Player getPlayer()
	{
		return player;
	}

	public LocalPortal getPortal()
	{
		return portal;
	}

	public RenderTaskMode getMode()
	{
		return mode;
	}

	public boolean hasStarted()
	{
		return hasStarted;
	}
}
