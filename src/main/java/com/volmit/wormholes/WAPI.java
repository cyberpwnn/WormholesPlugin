package com.volmit.wormholes;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.portal.RemotePortal;
import com.volmit.wormholes.util.GList;

public class WAPI
{
	public static List<Portal> getPortals()
	{
		return Wormholes.host.getPortals();
	}
	
	public static List<Portal> getLocalPortals()
	{
		return Wormholes.host.getLocalPortals();
	}
	
	public static List<Portal> getRemotePortals()
	{
		GList<Portal> p = new GList<Portal>();
		
		for(Portal i : getPortals())
		{
			if(i instanceof RemotePortal)
			{
				p.add(i);
			}
		}
		
		return p;
	}
	
	public static Portal getPortalAt(Location l)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getPane().contains(l))
			{
				return i;
			}
		}
		
		return null;
	}
	
	public static Portal getPortalLookingAt(Player p)
	{
		return Wormholes.registry.getPortalLookingAt(p);
	}
	
	public static List<Portal> getPortalAreasAt(Location l)
	{
		return Wormholes.registry.getPortalsInView(l);
	}
	
	public static List<Portal> getPortalCloseAreaAt(Location l)
	{
		return Wormholes.registry.getPortalsInCloseView(l);
	}
	
	public static Portal getPortalByKey(PortalKey key)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getKey().equals(key))
			{
				return i;
			}
		}
		
		return null;
	}
	
	public static boolean hasBungeecordConnection()
	{
		return Wormholes.bus.isOnline();
	}
	
	public static String getServerName()
	{
		return Wormholes.bus.getServerName();
	}
	
	public static void fxShockPortal(LocalPortal p)
	{
		Wormholes.fx.strike(p);
	}
	
	public static void fxShockAllPortal(LocalPortal p)
	{
		Wormholes.fx.strikeAll(p);
	}
}
