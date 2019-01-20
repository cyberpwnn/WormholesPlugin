package com.volmit.wormholes.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.volmit.wormholes.portal.PortalPosition;
import com.volmit.wormholes.util.lang.Cuboid;
import com.volmit.wormholes.util.lang.Direction;
import com.volmit.wormholes.util.lang.GList;
import com.volmit.wormholes.util.lang.VectorMath;

public class Frustum
{
	private Cuboid region;
	private Cuboid clip;
	private Location origin;
	private GeoPolygonProc poly;

	public Frustum(Location iris, PortalPosition pp, int rr)
	{
		origin = iris;
		double distanceToPortal = iris.distance(pp.getCenter());
		double range = rr + (rr / (distanceToPortal + 1));
		Vector tl = VectorMath.direction(iris, pp.getCornerUL());
		Vector tr = VectorMath.direction(iris, pp.getCornerUR());
		Vector bl = VectorMath.direction(iris, pp.getCornerDL());
		Vector br = VectorMath.direction(iris, pp.getCornerDR());
		Location ptl = pp.getCornerUL().clone().add(tl.multiply(range));
		Location ptr = pp.getCornerUR().clone().add(tr.multiply(range));
		Location pbl = pp.getCornerDL().clone().add(bl.multiply(range));
		Location pbr = pp.getCornerDR().clone().add(br.multiply(range));
		poly = new GeoPolygonProc(new GeoPolygon(new GList<GeoPoint>().qadd(nGeoPoint(ptl)).qadd(nGeoPoint(ptr)).qadd(nGeoPoint(pbl)).qadd(nGeoPoint(pbr)).qadd(nGeoPoint(pp.getCornerUL())).qadd(nGeoPoint(pp.getCornerUR())).qadd(nGeoPoint(pp.getCornerDL())).qadd(nGeoPoint(pp.getCornerDR()))));
		region = new Cuboid(ptl, pp.getCornerDR()).getBoundingCuboid(new Cuboid(pbr, pp.getCornerUL()));

		for(Direction i : Direction.udnews())
		{
			region = region.expand(i.f(), 1);
		}

		for(Direction i : Direction.values())
		{
			clip = region.getFace(i.f());
			if(clip.contains(pp.getCenter()))
			{
				break;
			}
		}
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
