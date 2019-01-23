package com.volmit.wormholes.util.lang;

public class Intersection
{
	static class MVector3
	{
		public float x, y, z;
		
		public MVector3(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public MVector3 add(MVector3 other)
		{
			return new MVector3(x + other.x, y + other.y, z + other.z);
		}
		
		public MVector3 sub(MVector3 other)
		{
			return new MVector3(x - other.x, y - other.y, z - other.z);
		}
		
		public MVector3 scale(float f)
		{
			return new MVector3(x * f, y * f, z * f);
		}
		
		public MVector3 cross(MVector3 other)
		{
			return new MVector3(y * other.z - z * other.y, z - other.x - x * other.z, x - other.y - y * other.x);
		}
		
		public float dot(MVector3 other)
		{
			return x * other.x + y * other.y + z * other.z;
		}
	}
	
	public static boolean intersectRayWithSquare(MVector3 R1, MVector3 R2, MVector3 S1, MVector3 S2, MVector3 S3)
	{
		MVector3 dS21 = S2.sub(S1);
		MVector3 dS31 = S3.sub(S1);
		MVector3 n = dS21.cross(dS31);
		MVector3 dR = R1.sub(R2);
		
		float ndotdR = n.dot(dR);
		
		if(Math.abs(ndotdR) < 1e-6f)
		{
			return false;
		}
		
		float t = -n.dot(R1.sub(S1)) / ndotdR;
		MVector3 M = R1.add(dR.scale(t));
		MVector3 dMS1 = M.sub(S1);
		float u = dMS1.dot(dS21);
		float v = dMS1.dot(dS31);
		
		return (u >= 0.0f && u <= dS21.dot(dS21) && v >= 0.0f && v <= dS31.dot(dS31));
	}
}