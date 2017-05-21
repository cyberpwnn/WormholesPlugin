package org.cyberpwn.vortex.event;

import org.bukkit.entity.Player;
import org.cyberpwn.vortex.portal.Portal;

public class PortalPlayerEvent extends PortalEvent
{
	private final Player player;
	
	public PortalPlayerEvent(Portal portal, Player player)
	{
		super(portal);
		
		this.player = player;
	}
	
	public Player getPlayer()
	{
		return player;
	}
}
