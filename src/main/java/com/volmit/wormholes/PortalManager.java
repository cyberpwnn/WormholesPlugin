package com.volmit.wormholes;

import java.util.UUID;

import org.bukkit.event.Listener;

import com.volmit.wormholes.portal.ILocalPortal;
import com.volmit.wormholes.portal.IPortal;
import com.volmit.wormholes.util.lang.GList;
import com.volmit.wormholes.util.lang.GMap;

public class PortalManager implements Listener
{
	private GMap<UUID, ILocalPortal> portals;

	public PortalManager()
	{
		portals = new GMap<>();
	}

	public void updateLocalPortals()
	{
		for(ILocalPortal i : getLocalPortals())
		{
			updateLocalPortal(i);
		}
	}

	private void updateLocalPortal(ILocalPortal i)
	{
		i.update();
	}

	public GList<ILocalPortal> getLocalPortals()
	{
		return portals.v();
	}

	public boolean hasLocalPortal(UUID id)
	{
		return portals.containsKey(id);
	}

	public boolean hasLocalPortal(IPortal portal)
	{
		return hasLocalPortal(portal.getId());
	}

	public void addLocalPortal(ILocalPortal portal)
	{
		if(!hasLocalPortal(portal))
		{
			portals.put(portal.getId(), portal);
		}
	}

	public void removeLocalPortal(UUID portal)
	{
		portals.remove(portal);
	}

	public void removeLocalPortal(IPortal portal)
	{
		removeLocalPortal(portal.getId());
	}
}
