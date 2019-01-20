package com.volmit.wormholes.portal.shape;

import org.bukkit.World;

import com.volmit.wormholes.util.lang.BVector;
import com.volmit.wormholes.util.lang.Cuboid;
import com.volmit.wormholes.util.lang.Direction;

public class PortalStructure
{
	private BVector center;
	private BVector[] positions;
	private World world;
	private Direction direction;

	public Cuboid getPlane()
	{
		Cuboid c = new Cuboid(center.toLocation(getWorld()));

		for(BVector i : positions)
		{
			c = c.getBoundingCuboid(new Cuboid(center.toLocation(world, i)));
		}

		return c;
	}

	public BVector getCenter()
	{
		return center;
	}

	public void setCenter(BVector center)
	{
		this.center = center;
	}

	public BVector[] getPositions()
	{
		return positions;
	}

	public void setPositions(BVector[] positions)
	{
		this.positions = positions;
	}

	public World getWorld()
	{
		return world;
	}

	public void setWorld(World world)
	{
		this.world = world;
	}

	public Direction getDirection()
	{
		return direction;
	}

	public void setDirection(Direction direction)
	{
		this.direction = direction;
	}
}
