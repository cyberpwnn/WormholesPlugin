package com.volmit.wormholes.portal;

public interface IWormholePortal extends ILocalPortal
{
	public boolean isProjecting();

	public void setProjecting(boolean projecting);
}
