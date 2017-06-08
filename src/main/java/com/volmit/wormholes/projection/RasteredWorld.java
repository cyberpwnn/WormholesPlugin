package com.volmit.wormholes.projection;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;

public class RasteredWorld
{
	private GMap<Chunk, RasteredChunk> chunks;
	private World world;
	
	public RasteredWorld(World world)
	{
		chunks = new GMap<Chunk, RasteredChunk>();
		this.world = world;
	}
	
	public void flush()
	{
		for(Chunk j : chunks.k())
		{
			for(Player i : world.getPlayers())
			{
				chunks.get(j).project(i);
			}
			
			chunks.remove(j);
		}
	}
	
	public boolean hasChunks()
	{
		return !chunks.isEmpty();
	}
	
	public void queue(Location l, MaterialBlock mb)
	{
		createChunkIfNull(l.getChunk().getX(), l.getChunk().getZ());
		chunks.get(l.getChunk()).put(l.getBlockX(), l.getBlockY(), l.getBlockZ(), mb);
	}
	
	public void createChunkIfNull(int x, int z)
	{
		if(!chunks.containsKey(world.getChunkAt(x, z)))
		{
			chunks.put(world.getChunkAt(x, z), new RasteredChunk(x, z, world));
		}
	}
}
