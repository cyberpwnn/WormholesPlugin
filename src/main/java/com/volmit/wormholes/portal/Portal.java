package com.volmit.wormholes.portal;

import java.util.UUID;

import org.bukkit.util.Vector;

import com.volmit.wormholes.util.Direction;

public abstract class Portal implements IPortal
{
	protected Direction direction;
	private final UUID id;
	private final Vector origin;
	private String name;

	public Portal(UUID id, Vector origin)
	{
		this.id = id;
		this.origin = origin;
		direction = Direction.N;
		name = "Portal " + id.toString().substring(0, 4);
	}

	@Override
	public Vector getOrigin()
	{
		return origin;
	}

	@Override
	public UUID getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public Direction getDirection()
	{
		return direction;
	}
}
