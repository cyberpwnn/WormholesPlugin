package com.volmit.wormholes.project;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.volmit.wormholes.util.J;

public class ChunkWait
{
	public static Chunk getChunk(World w, int x, int z)
	{
		Chunk[] c = new Chunk[1];

		if(!Bukkit.isPrimaryThread())
		{
			J.s(() -> c[0] = w.getChunkAt(x, z));

			while(c[0] == null)
			{
				try
				{
					Thread.sleep(25);
					J.s(() -> c[0] = w.getChunkAt(x, z));
				}

				catch(InterruptedException e)
				{
					e.printStackTrace();
					break;
				}
			}

			return c[0];
		}

		return w.getChunkAt(x, z);
	}
}
