package com.volmit.wormholes.project;

import org.bukkit.entity.Player;

import com.volmit.wormholes.geometry.Frustum4D;
import com.volmit.wormholes.nms.ShadowQueue;
import com.volmit.wormholes.portal.IWormholePortal;

public interface IProjector
{
	public ProjectionMatrix getLocalMatrix();

	public ProjectionMatrix getRemoteMatrix();

	public Frustum4D getFrustum();

	public Frustum4D getLastFrustum();

	public ShadowQueue getQueue();

	public IWormholePortal getPortal();

	public void swapBuffers(Frustum4D newFrustum);

	public void initNewProjection();

	public void project();

	public Player getObserver();

	public void close();
}
