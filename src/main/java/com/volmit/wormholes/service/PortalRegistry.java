package com.volmit.wormholes.service;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.projection.ProjectionSet;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;

public class PortalRegistry
{
	protected GList<Portal> destroyQueue;
	protected GList<Portal> localPortals;
	protected GMap<String, GList<Portal>> mutexPortals;
	
	public PortalRegistry()
	{
		DB.d(this, "Starting Portal Registry");
		localPortals = new GList<Portal>();
		destroyQueue = new GList<Portal>();
		mutexPortals = new GMap<String, GList<Portal>>();
	}
	
	public GList<Portal> getDestroyQueue()
	{
		return destroyQueue;
	}
	
	public GList<Portal> getLocalPortals()
	{
		return localPortals;
	}
	
	public boolean hasPortalsInView(Location l)
	{
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(l))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public Portal getClosestViewedPortal(Location l)
	{
		GList<Portal> p = getPortalsInView(l);
		
		if(p.isEmpty())
		{
			return null;
		}
		
		double max = Double.MAX_VALUE;
		Portal c = null;
		
		for(Portal i : p)
		{
			double d = i.getPosition().getCenter().distance(l);
			
			if(d < max)
			{
				max = d;
				c = i;
			}
		}
		
		return c;
	}
	
	public GList<Portal> getPortalsInView(Location l)
	{
		GList<Portal> portals = new GList<Portal>();
		
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getArea().contains(l))
			{
				portals.add(i);
			}
		}
		
		return portals;
	}
	
	public GList<Portal> getPortalsInCloseView(Location l)
	{
		GList<Portal> portals = new GList<Portal>();
		
		for(Portal i : getLocalPortals())
		{
			if(i.getPosition().getIarea().contains(l))
			{
				portals.add(i);
			}
		}
		
		return portals;
	}
	
	public GMap<String, GList<Portal>> getMutexPortals()
	{
		return mutexPortals;
	}
	
	public ProjectionSet getOtherLocalPortals(Portal local)
	{
		ProjectionSet set = new ProjectionSet();
		
		for(Portal i : getLocalPortals())
		{
			if(!i.equals(local))
			{
				set.add(i.getPosition().getArea());
			}
		}
		
		return set;
	}
	
	public boolean isLookingAt(Player p, Portal portal)
	{
		return portal instanceof LocalPortal && ((LocalPortal) portal).isPlayerLookingAt(p);
	}
	
	public Portal getPortalLookingAt(Player p)
	{
		for(Portal i : Wormholes.host.getLocalPortals())
		{
			if(((LocalPortal) i).isPlayerLookingAt(p))
			{
				return i;
			}
		}
		
		return null;
	}
}
