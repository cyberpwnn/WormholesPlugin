package com.volmit.wormholes.geometry;

import org.bukkit.util.Vector;

public class Triangles
{
	public static boolean checkPointInTriangle(Vector p1, Vector p2, Vector p3, Vector point)
	{
		float angles = 0;
		Vector v1 = point.clone().subtract(p1).normalize();
		Vector v2 = point.clone().subtract(p2).normalize();
		Vector v3 = point.clone().subtract(p3).normalize();
		angles += Math.acos(v1.dot(v2));
		angles += Math.acos(v2.dot(v3));
		angles += Math.acos(v3.dot(v1));

		return (Math.abs(angles - 2 * Math.PI) <= 0.005);
	}

}
