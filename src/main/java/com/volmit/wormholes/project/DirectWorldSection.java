package com.volmit.wormholes.project;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.volmit.wormholes.nms.ShadowQueue;
import com.volmit.wormholes.util.MaterialBlock;

public class DirectWorldSection implements IWorldSection
{
	private int y;
	private Chunk c;

	public DirectWorldSection(Chunk c, int y)
	{
		this.y = y;
		this.c = c;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void queue(ShadowQueue q)
	{
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				for(int k = 0; k < 16; k++)
				{
					MaterialBlock mb = getType(i, j, k);
					int xx = (c.getX() << 4) + i;
					int yy = (y << 4) + j;
					int zz = (c.getZ() << 4) + k;
					q.setBlock(xx, yy, zz, mb.getMaterial().getId(), mb.getData());
					q.setBlockLight(xx, yy, zz, getBlockLight(i, j, k));
					q.setSkyLight(xx, yy, zz, getSkyLight(i, j, k));
				}
			}
		}
	}

	@Override
	public void absorb(World w)
	{
		// A little bird told me we already know how to get this information
	}

	@Override
	public byte getBlockLight(int x, int y, int z)
	{
		return (byte) (c.getBlock(x, (this.y >> 4) + y, z).getLightFromBlocks());
	}

	@Override
	public byte getSkyLight(int x, int y, int z)
	{
		return (byte) (c.getBlock(x, (this.y >> 4) + y, z).getLightFromSky());
	}

	@Override
	public MaterialBlock getType(int x, int y, int z)
	{
		return new MaterialBlock(c.getBlock(x, (this.y >> 4) + y, z));
	}
}
