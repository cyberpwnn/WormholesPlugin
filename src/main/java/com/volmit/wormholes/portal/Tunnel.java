package com.volmit.wormholes.portal;

public abstract class Tunnel implements ITunnel
{
	private final IPortal portal;
	private final TunnelType type;

	public Tunnel(IPortal destination, TunnelType type)
	{
		this.portal = destination;
		this.type = type;
	}

	@Override
	public IPortal getDestination()
	{
		return portal;
	}

	@Override
	public TunnelType getTunnelType()
	{
		return type;
	}

	@Override
	public abstract void push(Traversive t);
}
