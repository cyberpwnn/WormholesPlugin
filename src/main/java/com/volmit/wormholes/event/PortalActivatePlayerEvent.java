package com.volmit.wormholes.event;

import org.bukkit.entity.Player;
import com.volmit.wormholes.portal.Portal;

public class PortalActivatePlayerEvent extends PortalPlayerEvent
{
	public PortalActivatePlayerEvent(Portal portal, Player player)
	{
		super(portal, player);
	}
}
