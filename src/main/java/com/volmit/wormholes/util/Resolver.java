package com.volmit.wormholes.util.lang;

@SuppressWarnings("hiding")
@FunctionalInterface
public interface Resolver<K, V>
{
	public V resolve(K k);
}
