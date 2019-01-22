package com.volmit.wormholes.portal;

import java.util.UUID;

public interface IPortal
{
	public static final UUID NO_DESTINATION = UUID.nameUUIDFromBytes("NoDestination".getBytes());

	public UUID getId();

	public UUID getDestination();

	public void setDestination(UUID destination);

	public void clearDestination();

	public boolean hasDestination();

	public String getName();

	public void setName(String name);
}
