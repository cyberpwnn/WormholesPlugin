package com.volmit.wormholes.service;

import org.bukkit.event.Listener;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.renderer.PortalRenderer;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.GMap;

public class ProjectionService implements Listener
{
	private GMap<PortalKey, ProjectionPlane> remotePlanes;
	private Boolean projecting;
	private PortalRenderer renderer;

	public ProjectionService()
	{
		DB.d(this, "Starting Projection Service");
		renderer = new PortalRenderer();
		projecting = false;
		remotePlanes = new GMap<PortalKey, ProjectionPlane>();
	}

	public void flush()
	{
		if(!projecting && Settings.ENABLE_PROJECTIONS)
		{
			projecting = true;

			new A()
			{
				@Override
				public void async()
				{
					renderer.render(250);
					projecting = false;
				}
			};
		}
	}

	public GMap<PortalKey, ProjectionPlane> getRemotePlanes()
	{
		return remotePlanes;
	}

	public void deproject(LocalPortal l)
	{
		// TODO Auto-generated method stub

	}
}
