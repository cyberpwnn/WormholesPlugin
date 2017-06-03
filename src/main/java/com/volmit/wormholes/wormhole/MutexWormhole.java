package com.volmit.wormholes.wormhole;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.event.WormholePushEntityEvent;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import wraith.Direction;
import wraith.PluginMessage;
import wraith.VectorMath;
import wraith.Wraith;

public class MutexWormhole extends BaseWormhole
{
	public MutexWormhole(LocalPortal source, Portal destination)
	{
		super(source, destination);
		
		getFilters().add(new WormholeEntityFilter(FilterPolicy.MUTEX, FilterMode.WHITELIST, EntityType.PLAYER));
	}
	
	@Override
	public void onPush(Entity e)
	{
		if(e instanceof Player && Wormholes.bus.isOnline())
		{
			Player p = (Player) e;
			Vector direction = e.getLocation().getDirection();
			Vector velocity = e.getVelocity();
			Vector entry = VectorMath.directionNoNormal(getSource().getPosition().getCenter(), e.getLocation());
			Direction closestDirection = Direction.closest(direction, getSource().getIdentity().getFront(), getSource().getIdentity().getBack());
			Direction closestVelocity = Direction.closest(velocity, getSource().getIdentity().getFront(), getSource().getIdentity().getBack());
			direction = closestDirection.equals(getSource().getIdentity().getFront()) ? closestDirection.angle(direction, getDestination().getIdentity().getFront()) : closestDirection.angle(direction, getDestination().getIdentity().getBack());
			entry = closestDirection.equals(getSource().getIdentity().getFront()) ? closestDirection.angle(entry, getDestination().getIdentity().getFront()) : closestDirection.angle(entry, getDestination().getIdentity().getBack());
			velocity = closestVelocity.equals(getSource().getIdentity().getFront()) ? closestVelocity.angle(velocity, getDestination().getIdentity().getFront()) : closestVelocity.angle(velocity, getDestination().getIdentity().getBack());
			entry = getSource().getIdentity().getFront().isVertical() ? new Vector(0, -1, 0) : entry;
			
			if(getSource().getIdentity().getFront().isVertical() && !getDestination().getIdentity().getFront().isVertical())
			{
				direction = velocity.clone();
			}
			
			Wraith.callEvent(new WormholePushEntityEvent(getDestination(), e));
			
			if(Settings.BUNGEECORD_SEND_ONLY)
			{
				new PluginMessage(Wormholes.instance, "ConnectOther", p.getName(), getDestination().getServer()).send();
				return;
			}
			
			getSource().getService().sendPlayerThrough(p, p.getUniqueId(), getSource(), getDestination(), velocity, direction, entry, new Runnable()
			{
				@Override
				public void run()
				{
					new PluginMessage(Wormholes.instance, "ConnectOther", p.getName(), getDestination().getServer()).send();
				}
			});
		}
	}
}
