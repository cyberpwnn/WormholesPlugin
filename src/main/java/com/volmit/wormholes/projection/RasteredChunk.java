package com.volmit.wormholes.projection;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Status;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.chunk.VirtualChunk;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.VersionBukkit;
import com.volmit.wormholes.wrapper.WrapperPlayServerMultiBlockChange;

public class RasteredChunk
{
	private int x;
	private int z;
	private MultiBlockChangeInfo[][][] mbi;
	private World world;
	private VirtualChunk cx;

	public RasteredChunk(int x, int z, World world, VirtualChunk cx)
	{
		this.world = world;
		this.x = x;
		this.z = z;
		this.cx = cx;

		mbi = new MultiBlockChangeInfo[16][256][16];
	}

	public void flush()
	{
		mbi = new MultiBlockChangeInfo[16][256][16];
	}

	public void put(int x, int y, int z, MaterialBlock b)
	{
		try
		{
			mbi[x - (this.x << 4)][y][z - (this.z << 4)] = new MultiBlockChangeInfo(new Location(world, x, y, z), WrappedBlockData.createData(b.getMaterial(), b.getData()));
		}

		catch(Exception e)
		{

		}
	}

	public void putRaw(int x, int y, int z, MaterialBlock b)
	{
		try
		{
			mbi[x][y][z] = new MultiBlockChangeInfo(new Location(world, x + (this.x << 4), y, z + (this.z << 4)), WrappedBlockData.createData(b.getMaterial(), b.getData()));
		}

		catch(Exception e)
		{

		}
	}

	public int project(Player p)
	{
		if(VersionBukkit.wc() || !Settings.USE_LIGHTMAPS)
		{
			return projectOld(p);
		}

		if(cx == null)
		{
			return projectOld(p);
		}

		try
		{
			for(int i = 0; i < 16; i++)
			{
				for(int k = 0; k < 16; k++)
				{
					for(int j = 0; j < 256; j++)
					{
						if(mbi[i][j][k] != null)
						{
							cx.set(i, j, k, new MaterialBlock(mbi[i][j][k]));
						}
					}
				}
			}

			cx.send(p);
		}

		catch(Exception e)
		{
			System.out.println("Failed to send chunk packet on MC " + Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")");
			e.printStackTrace();
		}

		return 0;
	}

	public int projectOld(Player p)
	{
		WrapperPlayServerMultiBlockChange w = new WrapperPlayServerMultiBlockChange();
		w.setChunk(new ChunkCoordIntPair(x, z));
		GList<MultiBlockChangeInfo> inf = new GList<MultiBlockChangeInfo>();
		int lf = 0;

		for(int i = 0; i < 16; i++)
		{
			for(int k = 0; k < 16; k++)
			{
				int hv = 0;

				for(int j = 0; j < 256; j++)
				{
					if(mbi[i][j][k] != null)
					{
						inf.add(mbi[i][j][k]);
						lf += hv;
						hv = 0;
					}

					else
					{
						hv++;
					}
				}
			}
		}

		int dist = 1;
		int size = 8 + (inf.size() * 12);
		int sv = inf.size();
		w.setRecords(inf.toArray(new MultiBlockChangeInfo[inf.size()]));

		if(Status.fdq)
		{
			try
			{
				ProtocolLibrary.getProtocolManager().sendServerPacket(p, w.getHandle());
			}

			catch(InvocationTargetException e)
			{
				System.out.println("Failed to send chunk packet on MC " + Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")");
				e.printStackTrace();
			}
		}

		else
		{
			Wormholes.provider.getRasterer().queueRaster(p, new QueuedChunk(size, dist, lf)
			{
				@Override
				public void run()
				{
					try
					{
						Status.lightFault += getLf();
						ProtocolLibrary.getProtocolManager().sendServerPacket(p, w.getHandle());
					}

					catch(InvocationTargetException e)
					{
						System.out.println("Failed to send chunk packet on MC " + Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")");
						e.printStackTrace();
					}
				}
			});
		}

		return sv;
	}
}
