package org.cyberpwn.vortex.network;

public enum CL
{
	L1,
	L2,
	L3;
	
	public String get()
	{
		return "VTXP" + toString();
	}
}
