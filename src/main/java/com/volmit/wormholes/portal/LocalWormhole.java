package com.volmit.wormholes.portal;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.event.WormholePushEntityEvent;
import com.volmit.wormholes.util.Area;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.Wraith;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityVelocity;

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
	public void onPush(Entity e, Location intercept)
	{
		Vector direction = e.getLocation().getDirection();
		Vector velocity = e instanceof Player ? Wormholes.host.getActualVector((Player) e) : e.getVelocity();
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

		if(e instanceof Projectile)
		{
			destination.setDirection(velocity.clone());
		}

		if(e instanceof Fireball)
		{
			((Fireball) e).setDirection(velocity);
		}

		Wormholes.fx.push(e, e.getVelocity(), (LocalPortal) getSource(), intercept);
		Vector vx = velocity.clone();
		e.setVelocity(vx);
		e.teleport(destination);
		e.setVelocity(vx);

		if(e.getType().equals(EntityType.PLAYER))
		{
			specialVelocity((Player) e, vx);
		}

		Area a = new Area(e.getLocation(), 12);

		for(Player i : a.getNearbyPlayers())
		{
			specialVelocity(i, vx, e);
		}

		Wormholes.fx.push(e, e.getVelocity(), (LocalPortal) getDestination(), e.getLocation());
	}

	public void specialVelocity(Player p, Vector v)
	{
		WrapperPlayServerEntityVelocity w = new WrapperPlayServerEntityVelocity();
		w.setEntityID(p.getEntityId());
		w.setVelocityX(v.getX());
		w.setVelocityY(v.getY());
		w.setVelocityZ(v.getZ());
		w.sendPacket(p);
	}

	public void specialVelocity(Player p, Vector v, Entity e)
	{
		WrapperPlayServerEntityVelocity w = new WrapperPlayServerEntityVelocity();
		w.setEntityID(e.getEntityId());
		w.setVelocityX(v.getX());
		w.setVelocityY(v.getY());
		w.setVelocityZ(v.getZ());
		w.sendPacket(p);
	}
}
