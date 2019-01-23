package com.volmit.wormholes.chunk;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.volmit.wormholes.util.MaterialBlock;

public interface VirtualChunk
{
	public void set(int x, int y, int z, int id, byte data);

	public void set(int x, int y, int z, MaterialBlock m);

	public MaterialBlock get(int x, int y, int z);

	public int getId(int x, int y, int z);

	public byte getData(int x, int y, int z);

	public int getX();

	public int getZ();

	public World getWorld();

	public Chunk getChunk();

	public void send(Player p);

	public void trickLight(Player p);

	public void setBlockLight(int i, int j, int k, byte block);

	public void setSkyLight(int i, int j, int k, byte sky);

	public void setBiome(int i, int k, Biome biome);
}
