package com.volmit.wormholes.portal;

import java.util.UUID;

import com.volmit.wormholes.util.Direction;

public interface IPortal
{
	public Direction getDirection();

	public UUID getId();

	public String getName();

	public void setName(String name);
}
