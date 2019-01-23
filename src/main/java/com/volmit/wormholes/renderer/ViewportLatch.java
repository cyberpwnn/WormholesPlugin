package com.volmit.wormholes.renderer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.projection.Viewport;

public class ViewportLatch
{
	private Viewport currentViewport;
	private Viewport lastViewport;
	private Player player;
	private LocalPortal portal;
	private Location lastPosition;

	public ViewportLatch(Player player, LocalPortal portal)
	{
		currentViewport = lastViewport = new Viewport(player, portal);
		this.player = player;
		this.portal = portal;
		this.lastPosition = player.getLocation();
	}

	public void update()
	{
		if(player.isSneaking())
		{
			return;
		}

		Location currentPosition = player.getLocation();

		if(currentPosition.getBlockX() == lastPosition.getBlockX() && currentPosition.getBlockY() == lastPosition.getBlockY() && currentPosition.getBlockZ() == lastPosition.getBlockZ())
		{
			return;
		}

		lastPosition = currentPosition;
		currentPosition = getPlayer().getLocation();
		lastViewport = currentViewport;
		currentViewport = new Viewport(player, portal);
	}

	public Viewport getCurrentViewport()
	{
		return currentViewport;
	}

	public Viewport getLastViewport()
	{
		return lastViewport;
	}

	public Player getPlayer()
	{
		return player;
	}

	public LocalPortal getPortal()
	{
		return portal;
	}

	public Location getLastPosition()
	{
		return lastPosition;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentViewport == null) ? 0 : currentViewport.hashCode());
		result = prime * result + ((lastPosition == null) ? 0 : lastPosition.hashCode());
		result = prime * result + ((lastViewport == null) ? 0 : lastViewport.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + ((portal == null) ? 0 : portal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(!(obj instanceof ViewportLatch))
		{
			return false;
		}
		ViewportLatch other = (ViewportLatch) obj;
		if(currentViewport == null)
		{
			if(other.currentViewport != null)
			{
				return false;
			}
		}
		else if(!currentViewport.equals(other.currentViewport))
		{
			return false;
		}
		if(lastPosition == null)
		{
			if(other.lastPosition != null)
			{
				return false;
			}
		}
		else if(!lastPosition.equals(other.lastPosition))
		{
			return false;
		}
		if(lastViewport == null)
		{
			if(other.lastViewport != null)
			{
				return false;
			}
		}
		else if(!lastViewport.equals(other.lastViewport))
		{
			return false;
		}
		if(player == null)
		{
			if(other.player != null)
			{
				return false;
			}
		}
		else if(!player.equals(other.player))
		{
			return false;
		}
		if(portal == null)
		{
			if(other.portal != null)
			{
				return false;
			}
		}
		else if(!portal.equals(other.portal))
		{
			return false;
		}
		return true;
	}
}
