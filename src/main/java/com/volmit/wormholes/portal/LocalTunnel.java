package com.volmit.wormholes.portal;

public class LocalTunnel extends Tunnel
{
	public LocalTunnel(LocalPortal portal)
	{
		super(portal, TunnelType.LOCAL);
	}

	@Override
	public void push(Traversive t)
	{
		// TODO Auto-generated method stub
	}
}
