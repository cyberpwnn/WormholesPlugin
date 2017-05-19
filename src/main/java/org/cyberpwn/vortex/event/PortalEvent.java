package org.cyberpwn.vortex.event;

import org.cyberpwn.vortex.portal.Portal;
import wraith.PhantomEvent;

public class PortalEvent extends PhantomEvent
{
	private Portal portal;
	
	public PortalEvent(Portal portal)
	{
		this.portal = portal;
	}
	
	public Portal getPortal()
	{
		return portal;
	}
}
