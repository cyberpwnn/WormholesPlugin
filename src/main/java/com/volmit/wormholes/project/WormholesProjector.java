package com.volmit.wormholes.project;

import org.bukkit.util.Vector;

import com.volmit.wormholes.geometry.Frustum4D;
import com.volmit.wormholes.nms.ShadowQueue;
import com.volmit.wormholes.util.AxisAlignedBB;
import com.volmit.wormholes.util.GSet;

public class WormholesProjector implements IProjector
{
	private ProjectionMatrix matrix;
	private Frustum4D frustum;
	private Frustum4D lastFrustum;
	private ShadowQueue queue;
	private GSet<Vector> lastSections;

	public WormholesProjector(ProjectionMatrix matrix, Frustum4D initial, ShadowQueue queue)
	{
		this.matrix = matrix;
		this.frustum = initial;
		this.lastFrustum = initial;
		this.queue = queue;
		lastSections = new GSet<>();
	}

	@Override
	public void project()
	{
		AxisAlignedBB current = getFrustum().getRegion();
		AxisAlignedBB last = getLastFrustum().getRegion();
		AxisAlignedBB area = new AxisAlignedBB(current);
		area.encapsulate(last);
	}

	@Override
	public void swapBuffers(Frustum4D newFrustum)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public ProjectionMatrix getMatrix()
	{
		return matrix;
	}

	@Override
	public Frustum4D getFrustum()
	{
		return frustum;
	}

	@Override
	public Frustum4D getLastFrustum()
	{
		return lastFrustum;
	}

	@Override
	public ShadowQueue getQueue()
	{
		return queue;
	}
}
