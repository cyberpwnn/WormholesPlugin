package com.volmit.wormholes.portal;

import com.volmit.wormholes.util.RemoteWorld;

public interface IRemotePortal extends IPortal
{
	public RemoteWorld getServer();
}
