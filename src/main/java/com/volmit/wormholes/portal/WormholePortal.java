package com.volmit.wormholes.portal;

import java.util.UUID;

public class WormholePortal extends LocalPortal implements IWormholePortal
{
	private boolean projecting;

	public WormholePortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id, type, structure);
		projecting = false;
	}

	@Override
	public void update()
	{
		super.update();

		if(isOpen() && isProjecting())
		{
			flushProjections();
		}
	}

	private void flushProjections()
	{
		// TODO mhm
	}

	@Override
	public boolean supportsProjections()
	{
		return false;
	}

	@Override
	public boolean isProjecting()
	{
		return projecting;
	}

	@Override
	public void setProjecting(boolean projecting)
	{
		this.projecting = projecting;
	}
}
