package com.volmit.wormholes.project;

import org.bukkit.entity.Player;

import com.volmit.wormholes.portal.IWormholePortal;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;

public interface IProjectionTracker
{
	public GMap<Player, IProjector> getTrackedProjectors();

	public GList<IProjector> getProjectors();

	public void startTracking(Player p);

	public void stopTracking(Player p);

	public void stopTracking();

	public IWormholePortal getPortal();
}
