package com.volmit.wormholes.project;

import com.volmit.wormholes.geometry.Frustum4D;

public interface IProjector
{
	public ProjectionMatrix getMatrix();

	public Frustum4D getFrustum();

}
