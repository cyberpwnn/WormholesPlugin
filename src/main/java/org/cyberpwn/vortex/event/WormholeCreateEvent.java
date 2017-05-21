package org.cyberpwn.vortex.event;

import org.cyberpwn.vortex.portal.Portal;

public class WormholeCreateEvent extends PortalEvent
{
	public WormholeCreateEvent(Portal portal)
	{
		super(portal);
	}
}
