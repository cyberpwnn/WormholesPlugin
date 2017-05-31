package com.volmit.wormholes.service;

import wraith.Timed;

public class TimingsService
{
	public static Timed root;
	public static Timed asyn;
	
	public TimingsService()
	{
		root = new Timed("wormholes", 0);
		asyn = new Timed("wormholes-async", 0);
	}
}
