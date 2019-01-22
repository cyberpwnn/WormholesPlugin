package com.volmit.wormholes.portal;

import java.util.UUID;

public interface Portal
{
	public UUID getId();

	public UUID getDestination();

	public void setDestination();

	public String getName();

	public void setName();
}
