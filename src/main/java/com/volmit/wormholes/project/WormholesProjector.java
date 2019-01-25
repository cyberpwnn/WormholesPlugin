package com.volmit.wormholes.project;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.geometry.Frustum4D;
import com.volmit.wormholes.nms.ShadowQueue;
import com.volmit.wormholes.portal.IPortal;
import com.volmit.wormholes.portal.IWormholePortal;
import com.volmit.wormholes.util.AxisAlignedBB;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MaterialBlock;

public class WormholesProjector implements IProjector
{
	private IWormholePortal portal;
	private Player observer;
	private ProjectionMatrix localMatrix;
	private ProjectionMatrix remoteMatrix;
	private Frustum4D frustum;
	private Frustum4D lastFrustum;
	private ShadowQueue queue;
	private GSet<Vector> lastSections;
	private GSet<Vector> currentSections;
	private DirectionalIterator it;
	private boolean ignoreLast;
	private boolean flushable;
	private long lastFlush;
	private int x;
	private int y;
	private int z;

	public WormholesProjector(IWormholePortal portal, IPortal remotePortal, Player observer, Frustum4D initial, ShadowQueue queue)
	{
		lastFlush = M.ms();
		flushable = false;
		this.observer = observer;
		ignoreLast = true;
		this.portal = portal;
		this.localMatrix = portal.getMatrix();
		this.remoteMatrix = remotePortal.getMatrix();
		this.frustum = initial;
		this.lastFrustum = initial;
		this.queue = queue;
		lastSections = new GSet<>();
		currentSections = new GSet<>();
	}

	@Override
	public void project()
	{
		if(it == null)
		{
			initNewProjection();
		}

		if(it.isDone())
		{
			flush();
			return;
		}

		if(it == null)
		{
			return;
		}

		while(!it.isDone())
		{
			it.computeNextLayer();
		}
	}

	private void flush()
	{
		if(flushable && M.ms() - lastFlush > Settings.PROJECTION_FLUSH_TIME)
		{
			flushable = false;
			lastFlush = M.ms();

			try
			{
				Wormholes.networkManager.queue(getObserver(), getQueue().dump());
			}

			catch(Throwable e)
			{

			}
		}
	}

	@Override
	public void initNewProjection()
	{
		AxisAlignedBB current = getFrustum().getRegion();
		AxisAlignedBB last = getLastFrustum().getRegion();
		AxisAlignedBB area = new AxisAlignedBB(current);
		area.encapsulate(last);
		Vector min = area.min();
		Vector max = area.max();
		max.setY(max.getY() > 255 ? 255 : max.getY());
		min.setY(min.getY() < 0 ? 0 : min.getY());
		area = new AxisAlignedBB(min, max);

		for(Vector i : lastSections)
		{
			if(!currentSections.contains(i))
			{
				queue.rebaseSection(i.getBlockX(), i.getBlockY(), i.getBlockZ());
				flushable = true;
			}
		}

		lastSections.clear();
		lastSections.addAll(currentSections);
		currentSections.clear();

		it = new DirectionalIterator(area, getFrustum().getDirection().reverse())
		{
			@Override
			public void process(int xx, int yy, int zz)
			{
				Vector v = new Vector(x, y, z);
				Vector c = new Vector(x >> 4, y >> 4, z >> 4);
				int mode = 0;

				if(getFrustum().contains(v))
				{
					mode = 1;

					if(lastFrustum.contains(v))
					{
						mode = ignoreLast ? mode : 0;
						currentSections.add(c);
					}
				}

				else if(lastFrustum.contains(v))
				{
					mode = ignoreLast ? mode : -1;
				}

				if(mode == 1)
				{
					currentSections.add(c);
					queue(v);
				}

				else if(mode == -1)
				{
					dequeue(v);
				}
			}
		};
	}

	private void queue(Vector v)
	{
		x = v.getBlockX();
		y = v.getBlockY();
		z = v.getBlockZ();
		MaterialBlock mb = getRemoteMatrix().getBlock(x, y, z, getPortal());
		getQueue().setBlock(x, y, z, mb.getId(), mb.getData());
		getQueue().setBlockLight(x, y, z, getRemoteMatrix().getBlockLight(x, y, z, getPortal()));
		getQueue().setSkyLight(x, y, z, getRemoteMatrix().getSkyLight(x, y, z, getPortal()));
		flushable = true;
	}

	private void dequeue(Vector v)
	{
		x = v.getBlockX();
		y = v.getBlockY();
		z = v.getBlockZ();
		MaterialBlock mb = getLocalMatrix().getBlock(x, y, z, getPortal());
		getQueue().setBlock(x, y, z, mb.getId(), mb.getData());
		getQueue().setBlockLight(x, y, z, getLocalMatrix().getBlockLight(x, y, z, getPortal()));
		getQueue().setSkyLight(x, y, z, getLocalMatrix().getSkyLight(x, y, z, getPortal()));
		flushable = true;
	}

	@Override
	public void swapBuffers(Frustum4D newFrustum)
	{
		if(it != null && !it.isDone())
		{
			return;
		}

		Vector a = lastFrustum.getIris().toVector();
		Vector b = newFrustum.getIris().toVector();

		if(a.toBlockVector().equals(b.toBlockVector()))
		{
			return;
		}

		lastFrustum = getFrustum();
		frustum = newFrustum;
		it = null;
		ignoreLast = false;
	}

	@Override
	public void close()
	{
		getQueue().rebaseAll();
		Wormholes.networkManager.queue(observer, getQueue().dump());
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

	@Override
	public IWormholePortal getPortal()
	{
		return portal;
	}

	@Override
	public ProjectionMatrix getLocalMatrix()
	{
		return localMatrix;
	}

	@Override
	public ProjectionMatrix getRemoteMatrix()
	{
		return remoteMatrix;
	}

	@Override
	public Player getObserver()
	{
		return observer;
	}
}
