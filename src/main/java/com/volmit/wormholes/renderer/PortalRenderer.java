package com.volmit.wormholes.renderer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.volume.lang.collections.GList;
import com.volmit.volume.lang.collections.GMap;
import com.volmit.volume.math.Profiler;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.LocalWormhole;
import com.volmit.wormholes.portal.MutexWormhole;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.projection.Viewport;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.VectorMath;

public class PortalRenderer implements Renderer
{
	private GMap<String, ViewportLatch> views;
	private GMap<String, Renderlet> renderers;
	private double msPerIteration;

	public PortalRenderer()
	{
		views = new GMap<String, ViewportLatch>();
		renderers = new GMap<String, Renderlet>();
		msPerIteration = 0.01;
	}

	protected String getId(Player p, LocalPortal portal)
	{
		return p.getUniqueId().toString() + ":" + portal.getDiskID().toString();
	}

	@Override
	public Viewport getViewport(Player p, LocalPortal portal)
	{
		if(!views.containsKey(getId(p, portal)))
		{
			views.put(getId(p, portal), new ViewportLatch(p, portal));
		}

		return views.get(getId(p, portal)).getCurrentViewport();
	}

	@Override
	public Viewport getLastViewport(Player p, LocalPortal portal)
	{
		if(!views.containsKey(getId(p, portal)))
		{
			views.put(getId(p, portal), new ViewportLatch(p, portal));
		}

		return views.get(getId(p, portal)).getLastViewport();
	}

	@Override
	public Renderlet getRenderlet(Player p, LocalPortal portal)
	{
		if(!renderers.containsKey(getId(p, portal)))
		{
			renderers.put(getId(p, portal), createNewRenderlet(p, portal, getViewport(p, portal), getLastViewport(p, portal)));
		}

		return renderers.get(getId(p, portal));
	}

	private Renderlet createNewRenderlet(Player p, LocalPortal portal, Viewport viewport, Viewport lastViewport)
	{
		Cuboid box = new Cuboid(viewport.getFrustum().getRegion()).getBoundingCuboid(lastViewport.getFrustum().getRegion());
		return new Renderlet(box, viewport.getDirection(), portal);
	}

	@Override
	public GList<Renderlet> getActiveRenderlets()
	{
		return renderers.v();
	}

	@Override
	public double getMsPerIteration()
	{
		return msPerIteration;
	}

	@Override
	public void render(double maxMs)
	{
		int maxIterations = (int) (maxMs / msPerIteration);
		GMap<Renderlet, String> rx = new GMap<Renderlet, String>();
		GMap<Renderlet, ProjectionPlane> rxv = new GMap<Renderlet, ProjectionPlane>();
		GMap<Renderlet, Portal> rxp = new GMap<Renderlet, Portal>();
		GList<Renderlet> rxr = new GList<Renderlet>();

		for(Portal i : Wormholes.host.getLocalPortals())
		{
			LocalPortal portal = (LocalPortal) i;
			ProjectionPlane s = null;

			if(portal.getWormhole() instanceof LocalWormhole)
			{
				LocalPortal dest = (LocalPortal) ((LocalWormhole)portal.getWormhole()).getDestination();
				s = dest.getProjectionPlane();
			}

			else if(portal.getWormhole() instanceof MutexWormhole)
			{
				s = Wormholes.getProjector().getRemotePlanes().get(portal.getKey());
			}

			if(s != null)
			{
				for(Player j : i.getPosition().getArea().getPlayers())
				{
					Renderlet r = getRenderlet(j, portal);

					if(r.isDone())
					{
						Viewport c = getViewport(j, portal);
						views.get(getId(j, portal)).update();
						if(!getViewport(j, portal).equals(c))
						{
							renderers.remove(getId(j, portal));
							r = getRenderlet(j, portal);
						}

						continue;
					}

					rx.put(r, getId(j, portal));
					rxv.put(r, s);
					rxp.put(r, i);
				}
			}

			else
			{
				System.out.println("s is null");
			}
		}

		if(rx.isEmpty())
		{
			return;
		}

		int maxPer = Math.max(50, maxIterations / rx.size());

		for(Renderlet i : rx.k())
		{
			rxr.add(i);
			Viewport current = views.get(rx.get(i)).getCurrentViewport();
			Viewport last = views.get(rx.get(i)).getLastViewport();
			ProjectionPlane plane = rxv.get(i);
			Portal p = rxp.get(i);

			new A()
			{
				@Override
				public void async()
				{
					Profiler pr = new Profiler();
					pr.begin();
					i.render(maxPer, (l) -> checkRender(l, current, last, plane, p));
					pr.end();

					if(Math.abs(pr.getMilliseconds() - pr.getMilliseconds()) > 0.001)
					{
						if(pr.getMilliseconds() > getMsPerIteration())
						{
							msPerIteration += (pr.getMilliseconds() - getMsPerIteration() / 100D);
						}

						else if(pr.getMilliseconds() < getMsPerIteration())
						{
							msPerIteration -= (getMsPerIteration() - pr.getMilliseconds() / 100D);
						}
					}
				}
			};
		}
	}

	protected void checkRender(Location l, Viewport current, Viewport last, ProjectionPlane plane, Portal p)
	{
		if(current.contains(l))
		{
			Vector dir = VectorMath.directionNoNormal(p.getPosition().getCenter(), l);
			Vector vec = dir.clone().add(new Vector(0.5, 0.5, 0.5));
			p.getIdentity().getFront().angle(vec, p.getIdentity().getFront());
			MaterialBlock mb = plane.getMapping().get(vec);

			if(mb == null)
			{
				return;
			}

			Wormholes.provider.getRasterer().queue(current.getP(), l, mb);
		}
	}
}
