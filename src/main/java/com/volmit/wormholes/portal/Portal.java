package com.volmit.wormholes.portal;

import java.util.UUID;

import com.volmit.wormholes.util.lang.Direction;

public class Portal implements IPortal
{
	protected Direction direction;
	private final UUID id;
	private String name;

	public Portal(UUID id)
	{
		this.id = id;
		direction = Direction.N;
		name = "Portal " + id.toString().substring(0, 4);
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
