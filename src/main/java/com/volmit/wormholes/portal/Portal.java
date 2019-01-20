package com.volmit.wormholes.portal;

import java.util.UUID;

import com.volmit.wormholes.portal.shape.PortalStructure;

public interface Portal
{
	public UUID getId();

	public String getName();

	public void setName();

	public PortalStructure getStructure();
}
