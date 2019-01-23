package com.volmit.wormholes.util;

public interface Operation extends Runnable
{
	public int getPriority();

	public String id();
}
