package com.volmit.wormholes.projection;

import org.bukkit.entity.Player;

import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.util.GMap;

public class VRMLock 
{
	private GMap<Portal, GMap<Player, VRM>> vrms;
	
	public VRMLock()
	{
		this.vrms = new GMap<Portal, GMap<Player, VRM>>();
	}
	
	public boolean hasVRM(Portal portal)
	{
		return vrms.containsKey(portal);
	}
	
	public boolean hasVRM(Portal portal, Player player)
	{
		return hasVRM(portal) && vrms.get(portal).containsKey(player);
	}
	
	public VRM getVRM(Portal portal, Player player)
	{
		if(hasVRM(portal, player))
		{
			return vrms.get(portal).get(player);
		}
		
		return null;
	}
	
	public void putVRM(Portal portal)
	{
		if(!vrms.containsKey(portal))
		{
			vrms.put(portal, new GMap<Player, VRM>());
		}
	}
	
	public void putVRM(Portal portal, Player player, VRM vrm)
	{
		putVRM(portal);
		vrms.get(portal).put(player, vrm);
	}
	
	public void dropVRM(Portal portal)
	{
		if(vrms.containsKey(portal))
		{
			vrms.remove(portal);
		}
	}
	
	public void dropVRM(Portal portal, Player player)
	{
		vrms.get(portal).remove(player);
	}
}
