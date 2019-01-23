package com.volmit.wormholes.network;

public enum CL
{
	L1,
	L2,
	L3,
	L4;
	
	public String get()
	{
		return "VTXP" + toString();
	}
}
