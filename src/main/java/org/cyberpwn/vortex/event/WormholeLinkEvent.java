package org.cyberpwn.vortex.event;

import org.cyberpwn.vortex.portal.Portal;

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
