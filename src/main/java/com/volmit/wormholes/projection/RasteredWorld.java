package com.volmit.wormholes.projection;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.volmit.wormholes.util.GMap;

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
				chunks.get(j).projectOlder(i);
			}
			
			chunks.remove(j);
		}
	}
	
	public boolean hasChunks()
	{
		return !chunks.isEmpty();
	}
}
