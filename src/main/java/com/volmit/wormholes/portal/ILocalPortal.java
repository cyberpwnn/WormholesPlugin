package com.volmit.wormholes.portal;

import com.volmit.wormholes.portal.shape.PortalStructure;

public interface ILocalPortal extends IPortal
{
	public PortalStructure getStructure();

	public PortalType getType();

	public void update();

	public void close();

	public boolean isOpen();

	public void open();

	public void setOpen(boolean open);

	public boolean isClosing();

	public boolean isOpening();

	public double getStateProgress();

	public String getState();

	public boolean isShowingProgress();
}
