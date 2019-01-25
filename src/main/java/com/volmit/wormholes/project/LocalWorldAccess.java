package com.volmit.wormholes.project;

import org.bukkit.World;

public class LocalWorldAccess extends WorldAccess
{
	private World world;

	public LocalWorldAccess(World world)
	{
		this.world = world;
	}

	@Override
	public IWorldSection cacheSection(int x, int y, int z)
	{
		return new DirectWorldSection(ChunkWait.getChunk(world, x, z), y);
	}

	@Override
	public void invalidateSection(int x, int y, int z)
	{
		// Direct buffers can never be invalid
	}

	@Override
	public void invalidate()
	{
		// Direct buffers can never be invalid
	}

	@Override
	public void invalidateBlock(int x, int y, int z)
	{
		// Direct buffers can never be invalid
	}
}
