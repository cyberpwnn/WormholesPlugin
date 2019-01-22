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
		this.xa = Math.min(xa, xb);
		this.xb = Math.max(xa, xb);
		this.ya = Math.min(ya, yb);
		this.yb = Math.max(ya, yb);
		this.za = Math.min(za, zb);
		this.zb = Math.max(za, zb);
	}

	public AxisAlignedBB(Cuboid c)
	{
		this(new AlignedPoint(c.getCornerVector(Direction.W, Direction.D, Direction.N)), new AlignedPoint(c.getCornerVector(Direction.E, Direction.U, Direction.S)));
	}

	public AxisAlignedBB(AlignedPoint a, AlignedPoint b)
	{
		this(a.getX(), b.getX(), a.getY(), b.getY(), a.getZ(), b.getZ());
	}

	public Vector getCornerVector(Direction x, Direction y, Direction z)
	{
		assert x.getAxis().equals(Axis.X) : " X direction must be on the X axis.";
		assert x.getAxis().equals(Axis.Y) : " Y direction must be on the Y axis.";
		assert x.getAxis().equals(Axis.Z) : " Z direction must be on the Z axis.";
		return new Vector(x.x() == 1 ? xb : xa, y.y() == 1 ? yb : ya, z.z() == 1 ? zb : za);
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
		return getFace(d, 0);
	}

	public AxisAlignedBB getFace(Direction d, double depth)
	{
		switch(d)
		{
			case D:
				return new AxisAlignedBB(xa, ya, za, xb, ya + depth, zb);
			case U:
				return new AxisAlignedBB(xa, yb - depth, za, xb, yb, zb);
			case N:
				return new AxisAlignedBB(xa, ya, za, xb, yb, za + depth);
			case S:
				return new AxisAlignedBB(xa, ya, zb - depth, xb, yb, zb);
			case E:
				return new AxisAlignedBB(xb, ya, za, xb - depth, yb, zb);
			case W:
				return new AxisAlignedBB(xa, ya, za, xa + depth, yb, zb);
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

	public double sizeX()
	{
		return xb - xa;
	}

	public double sizeY()
	{
		return yb - ya;
	}

	public double sizeZ()
	{
		return zb - za;
	}

	public double volume()
	{
		return sizeX() * sizeY() * sizeZ();
	}
}
