package com.volmit.wormholes.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.volmit.wormholes.portal.PortalPosition;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Cuboid.CuboidDirection;
import com.volmit.wormholes.util.VectorMath;

import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Vec4;

public class Frustum
{
	private Plane[] planes;
	private Cuboid region;
	private Cuboid clip;
	private double diff;

	public Frustum(Location iris, PortalPosition pp, int rr)
	{
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
		Plane pBack = Plane.fromPoints(toVec4(ptl.toVector()), toVec4(ptr.toVector()), toVec4(pbr.toVector()));
		Plane pFront = Plane.fromPoints(toVec4(pp.getCornerUL().toVector()), toVec4(pp.getCornerUR().toVector()), toVec4(pp.getCornerDR().toVector()));
		Plane pTop = Plane.fromPoints(toVec4(pp.getCornerUL().toVector()), toVec4(ptl.toVector()), toVec4(ptr.toVector()));
		Plane pBottom = Plane.fromPoints(toVec4(pp.getCornerDL().toVector()), toVec4(pbl.toVector()), toVec4(pbr.toVector()));
		Plane pLeft = Plane.fromPoints(toVec4(pp.getCornerUL().toVector()), toVec4(ptl.toVector()), toVec4(pbl.toVector()));
		Plane pRight = Plane.fromPoints(toVec4(pp.getCornerUR().toVector()), toVec4(ptr.toVector()), toVec4(pbr.toVector()));
		planes = new Plane[] {pBack, pTop, pFront, pLeft, pRight, pBottom};
		region = new Cuboid(ptl, pp.getCornerDR()).getBoundingCuboid(new Cuboid(pbr, pp.getCornerUL()));
		diff = Math.max(ptl.distance(pbr), pp.getCenter().distance(pbr)) * 1.5;

		for(CuboidDirection i : CuboidDirection.values())
		{
			clip = region.getFace(i);
			if(clip.contains(pp.getCenter()))
			{
				break;
			}
		}
	}

	public boolean intersects(Location l)
	{
		//@builder
		return region.contains(l)
				&& !clip.contains(l)
				&& intersects(l, l.clone().add(0, diff, diff))
				&& intersects(l, l.clone().add(diff, 0, diff))
				&& intersects(l, l.clone().add(diff, diff, 0))
				&& intersects(l, l.clone().add(0, 0, diff))
				&& intersects(l, l.clone().add(diff, 0, 0))
				&& intersects(l, l.clone().add(0, diff, 0))
				&& intersects(l, l.clone().add(0, -diff, -diff))
				&& intersects(l, l.clone().add(-diff, 0, -diff))
				&& intersects(l, l.clone().add(-diff, -diff, 0))
				&& intersects(l, l.clone().add(0, 0, -diff))
				&& intersects(l, l.clone().add(-diff, 0, 0))
				&& intersects(l, l.clone().add(0, -diff, 0))
				&& intersects(l, l.clone().add(diff, diff, diff))
				&& intersects(l, l.clone().add(-diff, -diff, -diff))
				&& intersects(l, l.clone().add(-diff, diff, -diff))
				&& intersects(l, l.clone().add(-diff, -diff, diff))
				&& intersects(l, l.clone().add(diff, -diff, -diff))
				&& intersects(l, l.clone().add(-diff, diff, diff))
				&& intersects(l, l.clone().add(diff, -diff, diff))
				&& intersects(l, l.clone().add(diff, diff, -diff));
		//@done
	}

	public boolean intersects(Location l, Location t)
	{
		for(Plane i : planes)
		{
			if(i.intersect(toVec4(l.toVector()), toVec4(t.toVector())) != null)
			{
				return true;
			}
		}

		return false;
	}

	public static Vec4 toVec4(Vector v)
	{
		return Vec4.fromArray3(new double[] {v.getX(), v.getY(), v.getZ()}, 0);
	}

	public Plane[] getPlanes()
	{
		return planes;
	}

	public Cuboid getRegion()
	{
		return region;
	}
}
