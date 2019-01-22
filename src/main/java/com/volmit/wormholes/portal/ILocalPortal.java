package com.volmit.wormholes.portal;

public interface ILocalPortal extends IPortal
{
	public PortalStructure getStructure();

	public PortalType getType();

	public void update();

	public void close();

	public boolean isOpen();

	public void open();

	public void setOpen(boolean open);
}
