package com.volmit.wormholes.project;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;

import com.volmit.wormholes.nms.ShadowQueue;
import com.volmit.wormholes.util.MaterialBlock;

public class BufferedWorldSection implements IWorldSection
{
	private int x;
	private int y;
	private int z;
	private int[] blocks;
	private byte[] light;

	public BufferedWorldSection(int x, int y, int z)
	{
		blocks = new int[4096];
		light = new byte[4096];
		this.x = x;
		this.y = y;
		this.z = z;
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
					int xx = (x << 4) + i;
					int yy = (y << 4) + j;
					int zz = (z << 4) + k;
					q.setBlock(xx, yy, zz, mb.getMaterial().getId(), mb.getData());
					q.setBlockLight(xx, yy, zz, getBlockLight(i, j, k));
					q.setSkyLight(xx, yy, zz, getSkyLight(i, j, k));
				}
			}
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void absorb(World w)
	{
		Chunk c = w.getChunkAt(x, z);
		boolean s = w.getEnvironment().equals(Environment.NORMAL);

		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				for(int k = 0; k < 16; k++)
				{
					Block b = c.getBlock(i, (y << 4) + j, k);
					blocks[j << 8 | k << 4 | i] = b.getTypeId() << 4 | (b.getData() & 15);
					light[j << 8 | k << 4 | i] = (byte) (b.getLightFromBlocks() << 4 | ((s ? b.getLightFromSky() : 15) & 15));
				}
			}
		}
	}

	@Override
	public byte getBlockLight(int x, int y, int z)
	{
		return (byte) (light[y << 8 | z << 4 | x] >> 4);
	}

	@Override
	public byte getSkyLight(int x, int y, int z)
	{
		return (byte) (light[y << 8 | z << 4 | x] & 15);
	}

	@Override
	@SuppressWarnings("deprecation")
	public MaterialBlock getType(int x, int y, int z)
	{
		return new MaterialBlock(Material.getMaterial(blocks[y << 8 | z << 4 | x] >> 4), (byte) (blocks[y << 8 | z << 4 | x] & 15));
	}
}
