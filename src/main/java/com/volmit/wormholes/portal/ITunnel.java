package com.volmit.wormholes.portal;

public interface ITunnel
{
	public IPortal getDestination();

	public TunnelType getTunnelType();

	public void push(Traversive t);

	public boolean isValid();
}
