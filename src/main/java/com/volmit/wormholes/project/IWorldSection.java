package com.volmit.wormholes.project;

import org.bukkit.World;

import com.volmit.wormholes.nms.ShadowQueue;
import com.volmit.wormholes.util.MaterialBlock;

public interface IWorldSection
{
	public void queue(ShadowQueue q);

	public void absorb(World w);

	public byte getBlockLight(int x, int y, int z);

	public byte getSkyLight(int x, int y, int z);

	public MaterialBlock getType(int x, int y, int z);
}
