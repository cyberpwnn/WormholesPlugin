package com.volmit.wormholes.portal;

import com.volmit.wormholes.Wormholes;

public class LocalTunnel extends Tunnel
{
	public LocalTunnel(ILocalPortal portal)
	{
		super(portal, TunnelType.LOCAL);
	}

	@Override
	public void push(Traversive t)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isValid()
	{
		return Wormholes.portalManager.hasLocalPortal(getDestination().getId());
	}
}
