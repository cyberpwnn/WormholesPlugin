package com.volmit.wormholes;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.wormholes.portal.IWormholePortal;
import com.volmit.wormholes.project.IProjector;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.J;

public class ProjectionManager implements Listener
{
	private GList<IProjector> projectors;

	public ProjectionManager()
	{
		projectors = new GList<>();
		J.ar(() -> flush(), 0);
	}

	private void flush()
	{
		for(IProjector i : projectors)
		{
			i.project();
		}
	}

	public void addProjector(IProjector p)
	{
		projectors.add(p);
	}

	public void removeProjector(IProjector p)
	{
		projectors.remove(p);
	}

	public void removeProjector(IWormholePortal portal)
	{
		for(IProjector i : projectors.copy())
		{
			if(i.getPortal().getId().equals(portal.getId()))
			{
				i.close();
				projectors.remove(i);
			}
		}
	}

	public void removeProjector(Player player)
	{
		for(IProjector i : projectors.copy())
		{
			if(i.getObserver().equals(player))
			{
				i.close();
				projectors.remove(i);
			}
		}
	}

	public void removeProjector(IWormholePortal portal, Player player)
	{
		for(IProjector i : projectors.copy())
		{
			if(i.getObserver().equals(player) && i.getPortal().getId().equals(portal.getId()))
			{
				i.close();
				projectors.remove(i);
			}
		}
	}
}
