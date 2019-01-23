package com.volmit.wormholes.portal;

import com.volmit.wormholes.Wormholes;

public class DimensionalTunnel extends Tunnel
{
	public DimensionalTunnel(ILocalPortal portal)
	{
		super(portal, TunnelType.DIMENSIONAL);
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
