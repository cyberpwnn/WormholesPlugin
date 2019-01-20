package com.volmit.wormholes.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.volmit.wormholes.portal.shape.PortalStructure;
import com.volmit.wormholes.util.lang.Cuboid;
import com.volmit.wormholes.util.lang.GList;

public class Frustum
{
	private Cuboid region;
	private Cuboid clip;
	private Location origin;
	private GeoPolygonProc poly;

	public Frustum(Location iris, PortalStructure pp, int rr)
	{
		origin = iris;
		double distanceToPortal = iris.distance(pp.getCenter().toLocation(iris.getWorld()));
		double range = rr + (rr / (distanceToPortal + 1));

		GList<GeoPoint> points = new GList<GeoPoint>();
		poly = new GeoPolygonProc(new GeoPolygon(points));
	}

	public boolean contains(Location l)
	{
		GeoPoint p = nGeoPoint(l);
		return poly.PointInside3DPolygon(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
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

	public Cuboid getRegion()
	{
		return region;
	}
}
