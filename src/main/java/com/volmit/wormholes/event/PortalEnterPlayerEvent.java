package com.volmit.wormholes.event;

import org.bukkit.entity.Player;
import com.volmit.wormholes.portal.Portal;

public class PortalEnterPlayerEvent extends PortalPlayerEvent
{
	public PortalEnterPlayerEvent(Portal portal, Player player)
	{
		super(portal, player);
	}
}
