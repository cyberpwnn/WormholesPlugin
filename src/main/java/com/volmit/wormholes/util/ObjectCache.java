package com.volmit.wormholes.util;

public class ObjectCache<T>
{
	private GSet<T> cache;
	
	public ObjectCache()
	{
		cache = new GSet<T>();
	}
	
	public void cache(T t)
	{
		cache.add(t);
	}
	
	public void destroy(T t)
	{
		cache.remove(t);
	}
}
