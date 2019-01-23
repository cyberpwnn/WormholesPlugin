package com.volmit.wormholes.geometry;

import org.bukkit.Location;

import com.volmit.wormholes.portal.PortalStructure;
import com.volmit.wormholes.util.lang.AxisAlignedBB;
import com.volmit.wormholes.util.lang.Direction;
import com.volmit.wormholes.util.lang.GList;

public class Frustum4D
{
	private GList<Frustum> frustums;
	private AxisAlignedBB region;

	public Frustum4D(Location iris, PortalStructure structure, int rr)
	{
		frustums = new GList<>();
		double distanceToPortal = iris.distance(structure.getCenter());
		double range = rr + (rr / (distanceToPortal + 1));

		// TODO optimize
		// TODO show 1-3 frustums, not all 6 every time
		for(Direction i : Direction.udnews())
		{
			frustums.add(new Frustum(iris, structure, i, range));
		}

		region = new AxisAlignedBB(frustums.get(0).getRegion());

		for(Frustum i : frustums)
		{
			region.encapsulate(i.getRegion());
		}
	}

	public boolean contains(Location p)
	{
		if(!getRegion().contains(p))
		{
			return false;
		}

		for(Frustum i : frustums)
		{
			if(i.contains(p))
			{
				return true;
			}
		}

		return false;
	}

	public AxisAlignedBB getRegion()
	{
		return region;
	}
}
