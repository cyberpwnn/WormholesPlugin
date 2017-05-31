package com.volmit.wormholes.event;

import com.volmit.wormholes.portal.Portal;
import wraith.CancellablePhantomEvent;

public class PortalEvent extends CancellablePhantomEvent
{
	private final Portal portal;
	
	public PortalEvent(Portal portal)
	{
		this.portal = portal;
	}
	
	public Portal getPortal()
	{
		return portal;
	}
}
