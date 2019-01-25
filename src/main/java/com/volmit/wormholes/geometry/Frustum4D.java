package com.volmit.wormholes.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.portal.PortalStructure;
import com.volmit.wormholes.util.AxisAlignedBB;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.VectorMath;

public class Frustum4D
{
	private GList<Frustum> frustums;
	private AxisAlignedBB region;
	private Location iris;
	private Direction d;

	public Frustum4D(Location iris, PortalStructure structure, int rr)
	{
		frustums = new GList<>();
		this.iris = iris;
		double distanceToPortal = iris.distance(structure.getCenter());
		double range = rr + (rr / (distanceToPortal + 1));
		Vector direction = VectorMath.reverse(VectorMath.direction(iris, structure.getCenter()));
		d = Direction.closest(direction);

		for(Direction i : Direction.values())
		{
			if(i.x() == 1 && direction.getX() > Settings.FRUSTUM_CULLING_RATIO)
			{
				frustums.add(new Frustum(iris, structure, i, range));
			}

			else if(i.x() == -1 && direction.getX() < Settings.FRUSTUM_CULLING_RATIO)
			{
				frustums.add(new Frustum(iris, structure, i, range));
			}

			else if(i.y() == 1 && direction.getY() > Settings.FRUSTUM_CULLING_RATIO)
			{
				frustums.add(new Frustum(iris, structure, i, range));
			}

			else if(i.y() == -1 && direction.getY() < Settings.FRUSTUM_CULLING_RATIO)
			{
				frustums.add(new Frustum(iris, structure, i, range));
			}

			else if(i.z() == 1 && direction.getZ() > Settings.FRUSTUM_CULLING_RATIO)
			{
				frustums.add(new Frustum(iris, structure, i, range));
			}

			else if(i.z() == -1 && direction.getZ() < Settings.FRUSTUM_CULLING_RATIO)
			{
				frustums.add(new Frustum(iris, structure, i, range));
			}
		}

		region = new AxisAlignedBB(frustums.get(0).getRegion());

		for(Frustum i : frustums)
		{
			region.encapsulate(i.getRegion());
		}
	}

	public boolean contains(Vector p)
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

	public Direction getDirection()
	{
		return d;
	}

	public Location getIris()
	{
		return iris;
	}
}
