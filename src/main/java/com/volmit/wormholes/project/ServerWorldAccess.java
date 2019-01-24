package com.volmit.wormholes.project;

import com.volmit.wormholes.util.RemoteWorld;

public class ServerWorldAccess extends WorldAccess
{
	@SuppressWarnings("unused")
	private RemoteWorld server;

	public ServerWorldAccess(RemoteWorld server)
	{
		this.server = server;
	}

	@Override
	public IWorldSection cacheSection(int x, int y, int z)
	{
		BufferedWorldSection s = new BufferedWorldSection(x, y, z);
		// TODO Get section from server?

		return s;
	}
}
