package com.volmit.wormholes.project;

import org.bukkit.entity.Player;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.geometry.Frustum4D;
import com.volmit.wormholes.nms.ShadowQueue;
import com.volmit.wormholes.portal.IWormholePortal;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;

public class ProjectionTracker implements IProjectionTracker
{
	private GMap<Player, IProjector> trackers;
	private IWormholePortal portal;

	public ProjectionTracker(IWormholePortal portal)
	{
		trackers = new GMap<>();
		this.portal = portal;
	}

	@Override
	public GMap<Player, IProjector> getTrackedProjectors()
	{
		return trackers;
	}

	@Override
	public GList<IProjector> getProjectors()
	{
		return getTrackedProjectors().v();
	}

	@Override
	public void startTracking(Player p)
	{
		if(getTrackedProjectors().containsKey(p))
		{
			Wormholes.instance.getLogger().warning("Projection Tracker already exists for " + p.getName() + " in portal " + getPortal().getName());
			return;
		}

		//@builder
		getTrackedProjectors().put(p, new WormholesProjector(
				getPortal(),
				getPortal().getTunnel().getDestination(),
				p,
				new Frustum4D(p.getEyeLocation(),
						getPortal().getStructure(),
						(int) Settings.PROJECTION_RANGE),
				new ShadowQueue(p, p.getWorld())));
		//@done

		Wormholes.projectionManager.addProjector(getTrackedProjectors().get(p));
		Wormholes.instance.getLogger().info("Projection Tracker for " + p.getName() + " bound to portal " + getPortal().getName());
	}

	@Override
	public void stopTracking(Player p)
	{
		if(getTrackedProjectors().containsKey(p))
		{
			Wormholes.projectionManager.removeProjector(getPortal(), p);
			getTrackedProjectors().remove(p);
			Wormholes.instance.getLogger().info("Projection Tracker for " + p.getName() + " unbound from portal " + getPortal().getName());
		}
	}

	@Override
	public void stopTracking()
	{
		Wormholes.projectionManager.removeProjector(getPortal());

		for(Player i : getTrackedProjectors().k())
		{
			stopTracking(i);
		}
	}

	@Override
	public IWormholePortal getPortal()
	{
		return portal;
	}
}
