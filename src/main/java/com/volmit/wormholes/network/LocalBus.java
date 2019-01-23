package com.volmit.wormholes.network;

public class LocalBus extends BaseBus
{
	@Override
	public void flush()
	{
		for(Transmission i : getOutbox())
		{
			cancel(i);
			inbox(i);
		}
	}
}
