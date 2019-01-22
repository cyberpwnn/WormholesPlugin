package com.volmit.wormholes.util.lang;

import org.bukkit.World;
import org.bukkit.util.Vector;

public class AxisAlignedBB
{
	private double xa;
	private double xb;
	private double ya;
	private double yb;
	private double za;
	private double zb;

	public AxisAlignedBB(double xa, double xb, double ya, double yb, double za, double zb)
	{
		this.xa = M.min(xa, xb);
		this.xb = M.max(xa, xb);
		this.ya = M.min(ya, yb);
		this.yb = M.max(ya, yb);
		this.za = M.min(za, zb);
		this.zb = M.max(za, zb);
	}

	public AxisAlignedBB(AlignedPoint a, AlignedPoint b)
	{
		this(a.getX(), b.getX(), a.getY(), b.getY(), a.getZ(), b.getZ());
	}

	public Cuboid toCuboid(World world)
	{
		return new Cuboid(min().toLocation(world), max().toLocation(world));
	}

	public Vector random()
	{
		return new Vector(M.rand(xa, xb), M.rand(ya, yb), M.rand(za, zb));
	}

	public Vector center()
	{
		return max().subtract(min());
	}

	public Vector max()
	{
		return new Vector(xb, yb, zb);
	}

	public Vector min()
	{
		return new Vector(xa, ya, za);
	}

	public AxisAlignedBB getFace(Direction d)
	{
		switch(d)
		{
			case D:
				return new AxisAlignedBB(xa, ya, za, xb, ya, zb);
			case U:
				return new AxisAlignedBB(xa, yb, za, xb, yb, zb);
			case N:
				return new AxisAlignedBB(xa, ya, za, xb, yb, za);
			case S:
				return new AxisAlignedBB(xa, ya, zb, xb, yb, zb);
			case E:
				return new AxisAlignedBB(xb, ya, za, xb, yb, zb);
			case W:
				return new AxisAlignedBB(xa, ya, za, xa, yb, zb);
		}

		return this;
	}

	public boolean contains(AlignedPoint p)
	{
		return p.getX() >= xa && p.getX() <= xb && p.getY() >= ya && p.getZ() <= yb && p.getZ() >= za && p.getZ() <= zb;
	}

	public boolean intersects(AxisAlignedBB s)
	{
		return this.xb >= s.xa && this.yb >= s.ya && this.zb >= s.za && s.xb >= this.xa && s.yb >= this.ya && s.zb >= this.za;
	}
}
