package org.cyberpwn.vortex.event;

import org.cyberpwn.vortex.portal.Portal;
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
