package com.volmit.wormholes.portal;

import java.util.UUID;

public interface IPortal
{
	public UUID getId();

	public UUID getDestination();

	public void setDestination();

	public String getName();

	public void setName();
}
