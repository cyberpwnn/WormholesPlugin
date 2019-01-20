package com.volmit.wormholes.util.lang;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class BVector
{
	private int x;
	private int y;
	private int z;

	public BVector(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Location toLocation(World world, BVector off)
	{
		return toLocation(world).add(off.toVector());
	}

	public Location toLocation(World world)
	{
		return new Location(world, x, y, z);
	}

	public Vector toVector()
	{
		return new Vector(x, y, z);
	}

	public BVector()
	{
		this(0, 0, 0);
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getZ()
	{
		return z;
	}

	public void setZ(int z)
	{
		this.z = z;
	}
}
