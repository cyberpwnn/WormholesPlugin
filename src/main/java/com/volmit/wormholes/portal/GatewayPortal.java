package com.volmit.wormholes.portal;

import java.util.UUID;

public class GatewayPortal extends WormholePortal implements IGatewayPortal
{
	public GatewayPortal(UUID id, PortalStructure structure)
	{
		super(id, PortalType.GATEWAY, structure);
	}

	@Override
	public boolean isGateway()
	{
		return false;
	}
}
