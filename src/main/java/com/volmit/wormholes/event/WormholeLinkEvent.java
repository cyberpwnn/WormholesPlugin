package com.volmit.wormholes.event;

import com.volmit.wormholes.portal.Portal;

public class WormholeLinkEvent extends PortalEvent
{
	private final Portal linked;
	
	public WormholeLinkEvent(Portal portal, Portal linked)
	{
		super(portal);
		
		this.linked = linked;
	}
	
	public Portal getLinked()
	{
		return linked;
	}
}
