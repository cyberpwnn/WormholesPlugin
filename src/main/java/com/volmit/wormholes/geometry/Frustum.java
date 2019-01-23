package com.volmit.wormholes.geometry;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.volmit.wormholes.portal.PortalStructure;
import com.volmit.wormholes.util.AxisAlignedBB;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GBiset;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.ParticleEffect;
import com.volmit.wormholes.util.VectorMath;

public class Frustum
{
	private Location origin;
	private GeoPolygonProc poly;
	private AxisAlignedBB region;

	public Frustum(Location iris, PortalStructure pp, Direction cubeFace, double range)
	{
		origin = iris;
		AxisAlignedBB face = pp.getArea().getFace(cubeFace);
		GList<Location> points = new GList<>();
		GList<Location> farPoints = new GList<>();
		switch(face.getThinAxis())
		{
			case X:
				points.add(face.getCornerVector(cubeFace, Direction.U, Direction.S).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(cubeFace, Direction.U, Direction.N).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(cubeFace, Direction.D, Direction.S).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(cubeFace, Direction.D, Direction.N).toLocation(iris.getWorld()));
				break;
			case Y:
				points.add(face.getCornerVector(Direction.E, cubeFace, Direction.S).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(Direction.E, cubeFace, Direction.N).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(Direction.W, cubeFace, Direction.S).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(Direction.W, cubeFace, Direction.N).toLocation(iris.getWorld()));
				break;
			case Z:
				points.add(face.getCornerVector(Direction.E, Direction.U, cubeFace).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(Direction.E, Direction.D, cubeFace).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(Direction.W, Direction.U, cubeFace).toLocation(iris.getWorld()));
				points.add(face.getCornerVector(Direction.W, Direction.D, cubeFace).toLocation(iris.getWorld()));
				break;
		}

		for(Location i : points)
		{
			farPoints.add(i.clone().add(VectorMath.direction(iris, i).multiply(range)));
		}

		points.addAll(farPoints);
		region = new AxisAlignedBB(points);
		GList<GeoPoint> p = new GList<>();

		for(Location i : points)
		{
			p.add(nGeoPoint(i));
		}

		poly = new GeoPolygonProc(new GeoPolygon(p));
		dpoly(farPoints, 1);
	}

	public void dpoly(GList<Location> locs, double jd)
	{
		GList<GBiset<Location, Location>> ignore = new GList<>();

		for(Location i : locs)
		{
			for(Location j : locs)
			{
				if(i.equals(j))
				{
					continue;
				}

				if(ignore.contains(new GBiset<Location, Location>(i, j)) || ignore.contains(new GBiset<Location, Location>(j, i)))
				{
					continue;
				}

				dline(i, j, jd);
				ignore.add(new GBiset<Location, Location>(i, j));
			}
		}
	}

	public void dline(Location start, Location finish, double jd)
	{
		new Raycast(start, finish, jd)
		{
			@Override
			public boolean shouldContinue(Location l)
			{
				ParticleEffect.FLAME.display(0f, 1, l, 120);
				return true;
			}
		};
	}

	public boolean ray(Location corner, GList<Block> blocks)
	{
		Block c = corner.getBlock();
		Raycast r = new Raycast(origin, corner, 0.25)
		{
			@Override
			public boolean shouldContinue(Location l)
			{
				if(blocks.contains(l.getBlock()) && !c.equals(l.getBlock()))
				{
					return false;
				}

				if(blocks.contains(l.getBlock()) && c.equals(l.getBlock()))
				{
					return finishSuccess();
				}

				return true;
			}
		};

		return r.hadSuccess();
	}

	public boolean contains(Location l)
	{
		if(!getRegion().contains(l))
		{
			return false;
		}

		GeoPoint p = nGeoPoint(l);
		return poly.PointInside3DPolygon(p.getX(), p.getY(), p.getZ());
	}

	public static GeoPoint toGeoPoint(Vector v)
	{
		return new GeoPoint(v.getX(), v.getY(), v.getZ());
	}

	public static Location normalize(Location origin, Location loc)
	{
		return loc.clone().subtract(origin);
	}

	public static Vec4 toVec4(Vector v)
	{
		return Vec4.fromArray3(new double[] {v.getX(), v.getY(), v.getZ()}, 0);
	}

	public GeoPoint nGeoPoint(Location v)
	{
		return toGeoPoint(normalize(origin, v).toVector());
	}

	public Vec4 nVec4(Location v)
	{
		return toVec4(normalize(origin, v).toVector());
	}

	public AxisAlignedBB getRegion()
	{
		return region;
	}
}
