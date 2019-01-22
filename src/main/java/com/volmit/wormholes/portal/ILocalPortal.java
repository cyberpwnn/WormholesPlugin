package com.volmit.wormholes.portal;

import com.volmit.wormholes.portal.shape.PortalStructure;

public interface ILocalPortal extends IPortal
{
	public PortalStructure getStructure();

	public PortalType getType();
}
