package com.volmit.wormholes.util.lang;

public abstract class S implements Runnable
{
	public S()
	{
		J.s(this);
	}

	public S(int delay)
	{
		J.s(this, delay);
	}
}
