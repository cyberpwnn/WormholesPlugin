package com.volmit.wormholes.geometry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.portal.PortalStructure;
import com.volmit.wormholes.util.lang.AxisAlignedBB;
import com.volmit.wormholes.util.lang.Direction;
import com.volmit.wormholes.util.lang.GList;
import com.volmit.wormholes.util.lang.VectorMath;

public class Frustum4D
{
	private GList<Frustum> frustums;
	private AxisAlignedBB region;

	public Frustum4D(Location iris, PortalStructure structure, int rr)
	{
		frustums = new GList<>();
		double distanceToPortal = iris.distance(structure.getCenter());
		double range = rr + (rr / (distanceToPortal + 1));
		Vector direction = VectorMath.reverse(VectorMath.direction(iris, structure.getCenter()));
		GList<Direction> d = new GList<>();

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

		for(Player j : Bukkit.getServer().getOnlinePlayers())
		{
			j.sendMessage("CullFace: " + d.toString(", ") + " (" + direction + ")");
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
