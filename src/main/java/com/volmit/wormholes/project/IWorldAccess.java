package com.volmit.wormholes.project;

import com.volmit.wormholes.util.MaterialBlock;

public interface IWorldAccess
{
	public boolean hasSection(int x, int y, int z);

	public IWorldSection getSection(int x, int y, int z);

	public void invalidateSection(int x, int y, int z);

	public void invalidate();

	public MaterialBlock getBlock(int x, int y, int z);

	public byte getSkyLight(int x, int y, int z);

	public byte getBlockLight(int x, int y, int z);

	public void invalidateBlock(int x, int y, int z);
}
