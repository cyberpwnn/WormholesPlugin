package org.cyberpwn.vortex.wormhole;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import wraith.Direction;
import wraith.PluginMessage;
import wraith.TaskLater;
import wraith.VectorMath;

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
		if(e instanceof Player && VP.bus.isOnline())
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
			
			getSource().getService().sendPlayerThrough(p.getUniqueId(), getSource(), getDestination(), velocity, direction, entry);
			
			new TaskLater(3)
			{
				@Override
				public void run()
				{
					new PluginMessage(VP.instance, "ConnectOther", p.getName(), getDestination().getServer()).send();
				}
			};
		}
	}
}
