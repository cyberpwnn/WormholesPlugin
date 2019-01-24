package com.volmit.wormholes;

import java.util.UUID;

import org.bukkit.event.Listener;

import com.volmit.wormholes.portal.ILocalPortal;
import com.volmit.wormholes.portal.IPortal;
import com.volmit.wormholes.portal.PortalType;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.J;

public class PortalManager implements Listener
{
	private GMap<UUID, ILocalPortal> portals;

	public PortalManager()
	{
		portals = new GMap<>();
		J.ar(() -> updateLocalPortals(), 0);
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
			Wormholes.registerListener(portal);
		}
	}

	public void removeLocalPortal(UUID portal)
	{
		if(portals.containsKey(portal))
		{
			Wormholes.unregisterListener(portals.get(portal));
		}

		portals.remove(portal);
	}

	public void removeLocalPortal(IPortal portal)
	{
		removeLocalPortal(portal.getId());
	}

	public int getTotalPortalCount()
	{
		return getLocalPortals().size();
	}

	public int getAccessableCount(PortalType t)
	{
		if(t.equals(PortalType.GATEWAY))
		{
			return getGatewayCount();
		}

		return getTotalPortalCount() - getGatewayCount();
	}

	public int getGatewayCount()
	{
		int g = 0;

		for(ILocalPortal i : portals.v())
		{
			if(i.isGateway())
			{
				g++;
			}
		}

		return g;
	}
}
