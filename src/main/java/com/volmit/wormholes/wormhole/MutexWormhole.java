package com.volmit.wormholes.wormhole;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.event.WormholePushEntityEvent;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.util.PluginMessage;
import com.volmit.wormholes.util.Wraith;

public class MutexWormhole extends BaseWormhole
{
	public MutexWormhole(LocalPortal source, Portal destination)
	{
		super(source, destination);
		
		getFilters().add(new WormholeEntityFilter(FilterPolicy.MUTEX, FilterMode.WHITELIST, EntityType.PLAYER));
	}
	
	@Override
	public void onPush(Entity e, Location intercept)
	{
		if(e instanceof Player && Wormholes.bus.isOnline())
		{
			Player p = (Player) e;
			Wraith.callEvent(new WormholePushEntityEvent(getDestination(), e));
			new PluginMessage(Wormholes.instance, "ConnectOther", p.getName(), getDestination().getServer()).send();
		}
	}
}
