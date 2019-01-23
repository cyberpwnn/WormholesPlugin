package com.volmit.wormholes.event;

import org.bukkit.entity.Player;
import com.volmit.wormholes.portal.Portal;

public class PortalDeactivatePlayerEvent extends PortalPlayerEvent
{
	public PortalDeactivatePlayerEvent(Portal portal, Player player)
	{
		super(portal, player);
	}
}
