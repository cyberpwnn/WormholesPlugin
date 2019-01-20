package com.volmit.wormholes.util.lang;

public interface Pool
{
	public void shove(Runnable op);

	public void shutDown();

	public void shutDownNow();
}
