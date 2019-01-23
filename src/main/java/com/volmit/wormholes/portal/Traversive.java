package com.volmit.wormholes.portal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.util.lang.Direction;

public class Traversive
{
	private final Object object;
	private final TraversableType type;
	private final Direction inDirection;
	private final Vector inVelocity;
	private final Vector inLook;

	public Traversive(Object o, TraversableType type, Direction inDirection, Vector inVelocity, Vector inLook)
	{
		this.object = o;
		this.type = type;
		this.inDirection = inDirection;
		this.inVelocity = inVelocity;
		this.inLook = inLook;
	}

	public Traversive(Player player, Direction inDirection, Vector inVelocity, Vector inLook)
	{
		this(player, TraversableType.PLAYER, inDirection, inVelocity, inLook);
	}

	public Traversive(Entity entity, Direction inDirection, Vector inVelocity, Vector inLook)
	{
		this(entity, TraversableType.ENTITY, inDirection, inVelocity, inLook);
	}

	public Vector getOutVelocity(Direction outDirection)
	{
		return getInDirection().angle(getInVelocity(), outDirection);
	}

	public Vector getOutLook(Direction outDirection)
	{
		return getInDirection().angle(getInLook(), outDirection);
	}

	public Direction getInDirection()
	{
		return inDirection;
	}

	public Vector getInVelocity()
	{
		return inVelocity;
	}

	public Vector getInLook()
	{
		return inLook;
	}

	public Object getObject()
	{
		return object;
	}

	public TraversableType getType()
	{
		return type;
	}
}
