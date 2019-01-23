package com.volmit.wormholes.event;

import com.volmit.wormholes.portal.Portal;

public class WormholeCreateEvent extends PortalEvent
{
	public WormholeCreateEvent(Portal portal)
	{
		super(portal);
	}
}
