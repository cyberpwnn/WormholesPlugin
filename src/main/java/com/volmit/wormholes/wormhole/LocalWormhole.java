package com.volmit.wormholes.wormhole;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.event.WormholePushEntityEvent;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import wraith.Direction;
import wraith.VectorMath;
import wraith.Wraith;

public class LocalWormhole extends BaseWormhole
{
	public LocalWormhole(LocalPortal source, Portal destination)
	{
		super(source, destination);
		
		if(!Settings.ALLOW_ENTITIES)
		{
			getFilters().add(new WormholeEntityFilter(FilterPolicy.LOCAL, FilterMode.WHITELIST, EntityType.PLAYER));
		}
	}
	
	@Override
	public void onPush(Entity e)
	{
		Vector direction = e.getLocation().getDirection();
		Vector velocity = e.getVelocity();
		Vector entry = VectorMath.directionNoNormal(getSource().getPosition().getCenter(), e.getLocation());
		Direction closestDirection = Direction.closest(direction, getSource().getIdentity().getFront(), getSource().getIdentity().getBack());
		Direction closestVelocity = Direction.closest(velocity, getSource().getIdentity().getFront(), getSource().getIdentity().getBack());
		Location destination = getDestination().getPosition().getCenter().clone();
		direction = closestDirection.equals(getSource().getIdentity().getFront()) ? closestDirection.angle(direction, getDestination().getIdentity().getFront()) : closestDirection.angle(direction, getDestination().getIdentity().getBack());
		entry = closestDirection.equals(getSource().getIdentity().getFront()) ? closestDirection.angle(entry, getDestination().getIdentity().getFront()) : closestDirection.angle(entry, getDestination().getIdentity().getBack());
		velocity = closestVelocity.equals(getSource().getIdentity().getFront()) ? closestVelocity.angle(velocity, getDestination().getIdentity().getFront()) : closestVelocity.angle(velocity, getDestination().getIdentity().getBack());
		destination = getSource().getIdentity().getFront().isVertical() ? destination.subtract(0, 1, 0) : destination.clone().add(entry).setDirection(direction);
		
		if(getSource().getIdentity().getFront().isVertical() && !getDestination().getIdentity().getFront().isVertical())
		{
			destination.setDirection(velocity.clone());
		}
		
		Wraith.callEvent(new WormholePushEntityEvent(getDestination(), e));
		
		e.teleport(destination);
		e.setVelocity(velocity);
		Wormholes.fx.push(e, e.getVelocity(), (LocalPortal) getDestination());
	}
}