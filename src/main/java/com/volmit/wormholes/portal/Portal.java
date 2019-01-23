package com.volmit.wormholes.portal;

import java.util.UUID;

public class Portal implements IPortal
{
	private final UUID id;
	private UUID destination;
	private String name;

	public Portal(UUID id)
	{
		this.id = id;
		destination = NO_DESTINATION;
		name = "Portal " + id.toString().substring(0, 4);
	}

	@Override
	public UUID getId()
	{
		return id;
	}

	@Override
	public UUID getDestination()
	{
		return destination;
	}

	@Override
	public void setDestination(UUID destination)
	{
		if(destination == null)
		{
			clearDestination();
			return;
		}

		this.destination = destination;
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
	public void clearDestination()
	{
		setDestination(NO_DESTINATION);
	}

	@Override
	public boolean hasDestination()
	{
		return !getDestination().equals(NO_DESTINATION);
	}
}
