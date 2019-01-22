package com.volmit.wormholes.portal;

import com.volmit.wormholes.portal.shape.PortalStructure;

public interface ILocalPoral extends IPortal
{
	public PortalStructure getStructure();

	public PortalType getType();
}
