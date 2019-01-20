package com.volmit.wormholes.util.lang;

public interface Operation extends Runnable
{
	public int getPriority();

	public String id();
}
