package com.volmit.wormholes.portal.shape;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import com.volmit.wormholes.util.lang.Cuboid;
import com.volmit.wormholes.util.lang.Direction;
import com.volmit.wormholes.util.lang.GMap;
import com.volmit.wormholes.util.lang.GSet;

public class PortalStructure
{
	private Cuboid area;
	private World world;
	private GMap<Direction, Cuboid> faceCache = new GMap<>();
	private GSet<Location> cornerCache;

	public World getWorld()
	{
		return world;
	}

	public void setWorld(World world)
	{
		this.world = world;
	}

	public Set<Location> getCorners()
	{
		if(cornerCache == null)
		{
			cornerCache = new GSet<Location>();
			cornerCache.add(corner(Direction.W, Direction.U, Direction.N));
			cornerCache.add(corner(Direction.W, Direction.U, Direction.S));
			cornerCache.add(corner(Direction.W, Direction.D, Direction.N));
			cornerCache.add(corner(Direction.W, Direction.D, Direction.S));
			cornerCache.add(corner(Direction.E, Direction.U, Direction.N));
			cornerCache.add(corner(Direction.E, Direction.U, Direction.S));
			cornerCache.add(corner(Direction.E, Direction.D, Direction.N));
			cornerCache.add(corner(Direction.E, Direction.D, Direction.S));
		}

		return cornerCache;
	}

	private Location corner(Direction x, Direction y, Direction z)
	{
		Vector v = getArea().getCornerVector(x, y, z);
		return new Location(getArea().getWorld(), v.getX(), v.getY(), v.getZ());
	}

	public Cuboid getFace(Direction face)
	{
		if(!faceCache.containsKey(face))
		{
			faceCache.put(face, getArea().getFace(face.f()));
		}

		return faceCache.get(face);
	}

	public Cuboid getArea()
	{
		return area;
	}

	public void setArea(Cuboid area)
	{
		this.area = area;
		invalidateCache();
	}

	private void invalidateCache()
	{
		faceCache.clear();
		cornerCache = null;
	}
}
