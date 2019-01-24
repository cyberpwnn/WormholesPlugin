package com.volmit.wormholes.project;

import com.volmit.wormholes.geometry.Frustum4D;
import com.volmit.wormholes.nms.ShadowQueue;

public interface IProjector
{
	public ProjectionMatrix getMatrix();

	public Frustum4D getFrustum();

	public Frustum4D getLastFrustum();

	public ShadowQueue getQueue();

	public void swapBuffers(Frustum4D newFrustum);

	public void project();
}
