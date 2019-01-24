package com.volmit.wormholes.project;

import org.bukkit.util.Vector;

import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;

public abstract class WorldAccess implements IWorldAccess
{
	private GMap<Vector, IWorldSection> sections;

	public WorldAccess()
	{
		sections = new GMap<>();
	}

	public abstract IWorldSection cacheSection(int x, int y, int z);

	@Override
	public void invalidateSection(int x, int y, int z)
	{
		sections.remove(new Vector(x, y, z));
	}

	@Override
	public void invalidate()
	{
		sections.clear();
	}

	@Override
	public void invalidateBlock(int x, int y, int z)
	{
		invalidateSection(x >> 4, y >> 4, z >> 4);
	}

	@Override
	public boolean hasSection(int x, int y, int z)
	{
		return sections.containsKey(new Vector(x, y, z));
	}

	@Override
	public IWorldSection getSection(int x, int y, int z)
	{
		if(!hasSection(x, y, z))
		{
			sections.put(new Vector(x, y, z), cacheSection(x, y, z));
		}

		return sections.get(new Vector(x, y, z));
	}

	@Override
	public MaterialBlock getBlock(int x, int y, int z)
	{
		return getSection(x >> 4, y >> 4, z >> 4).getType(x & 15, y & 15, z & 15);
	}

	@Override
	public byte getSkyLight(int x, int y, int z)
	{
		return getSection(x >> 4, y >> 4, z >> 4).getSkyLight(x & 15, y & 15, z & 15);
	}

	@Override
	public byte getBlockLight(int x, int y, int z)
	{
		return getSection(x >> 4, y >> 4, z >> 4).getBlockLight(x & 15, y & 15, z & 15);
	}
}
