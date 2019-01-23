package com.volmit.wormholes.util;

@SuppressWarnings("hiding")
@FunctionalInterface
public interface Resolver<K, V>
{
	public V resolve(K k);
}
