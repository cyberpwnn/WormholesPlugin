package com.volmit.wormholes.event;

import com.volmit.wormholes.portal.Portal;

public class WormholeUnlinkEvent extends PortalEvent
{
	public WormholeUnlinkEvent(Portal portal)
	{
		super(portal);
	}
}
