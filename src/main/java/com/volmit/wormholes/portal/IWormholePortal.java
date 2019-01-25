package com.volmit.wormholes.portal;

import org.bukkit.entity.Player;

import com.volmit.wormholes.project.IBoundingBoxTracker;
import com.volmit.wormholes.project.IProjectionTracker;
import com.volmit.wormholes.util.AxisAlignedBB;

public interface IWormholePortal extends ILocalPortal
{
	public boolean isProjecting();

	public void setProjecting(boolean projecting);

	public IProjectionTracker getTracker();

	public IBoundingBoxTracker<Player> getPlayerTracker();

	public AxisAlignedBB getView();
}
