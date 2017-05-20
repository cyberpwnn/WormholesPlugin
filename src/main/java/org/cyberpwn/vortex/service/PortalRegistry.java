package org.cyberpwn.vortex.service;

import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.projection.ProjectionSet;
import wraith.GList;
import wraith.GMap;

public class PortalRegistry
{
	protected GList<Portal> destroyQueue;
	protected GList<Portal> localPortals;
	protected GMap<String, GList<Portal>> mutexPortals;
	
	public PortalRegistry()
	{
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
}