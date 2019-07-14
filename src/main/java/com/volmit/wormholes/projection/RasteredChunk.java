package com.volmit.wormholes.projection;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.volmit.volume.bukkit.U;
import com.volmit.volume.bukkit.nms.NMSSVC;
import com.volmit.volume.bukkit.nms.adapter.AbstractChunk;
import com.volmit.volume.bukkit.task.S;
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
	private BlockProperties[][][] mbp;
	private World world;
	private VirtualChunk cx;
	private AbstractChunk as;
	private boolean mapped;

	public RasteredChunk(int x, int z, World world, VirtualChunk cx, AbstractChunk as)
	{
		mapped = false;
		this.as = as;
		this.world = world;
		this.x = x;
		this.z = z;
		this.cx = cx;
		mbi = new MultiBlockChangeInfo[16][256][16];
		mbp = new BlockProperties[16][256][16];
	}

	public void flush()
	{
		mbi = new MultiBlockChangeInfo[16][256][16];
		mbp = new BlockProperties[16][256][16];
	}

	public void put(int x, int y, int z, BlockProperties bp)
	{
		try
		{
			mbp[x - (this.x << 4)][y][z - (this.z << 4)] = bp;
		}

		catch(Exception e)
		{

		}
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

	@SuppressWarnings("deprecation")
	public int projectNew(Player p)
	{
		try
		{
			as.setSky(!p.getWorld().getEnvironment().equals(Environment.NETHER));

			for(int i = 0; i < 16; i++)
			{
				for(int k = 0; k < 16; k++)
				{
					for(int j = 0; j < 256; j++)
					{
						if(mbi[i][j][k] != null)
						{
							if(Wormholes.edgy)
							{
								as.setBiome(i, k, cx.getChunk().getWorld().getBiome((x * 16) + i, (z * 16) + k));
							}

							as.set(i, j, k, mbi[i][j][k].getData().getType().getId(), mbi[i][j][k].getData().getData());
						}

						if(mbp[i][j][k] != null && Wormholes.edgy)
						{
							as.setBlockLight(i, j, k, mbp[i][j][k].block);
							as.setSkyLight(i, j, k, mbp[i][j][k].sky);

							if(mbp[i][j][k].biome != null)
							{
								as.setBiome(i, k, mbp[i][j][k].biome);
							}
						}
					}
				}
			}

			U.getService(NMSSVC.class).sendChunkMap(as, p);
		}

		catch(Exception e)
		{
			System.out.println("Failed to send chunk packet on MC " + Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")");
			e.printStackTrace();
		}

		return 0;
	}

	public int projectNewFull(Player p)
	{
		try
		{
			as.setSky(!p.getWorld().getEnvironment().equals(Environment.NETHER));
			as.forceSendBiomes(true);

			for(int i = 0; i < 16; i++)
			{
				for(int k = 0; k < 16; k++)
				{
					for(int j = 0; j < 256; j++)
					{
						if(Wormholes.edgy)
						{
							as.setBlockLight(i, j, k, 0);
							as.setSkyLight(i, j, k, 15);
							as.setBiome(i, k, cx.getChunk().getWorld().getBiome((x * 16) + i, (z * 16) + k));
						}

						if(mbi[i][j][k] != null)
						{
							as.set(i, j, k, mbi[i][j][k].getData().getType().getId(), mbi[i][j][k].getData().getData());
						}

						if(mbp[i][j][k] != null && Wormholes.edgy)
						{
							as.setBlockLight(i, j, k, mbp[i][j][k].block);
							as.setSkyLight(i, j, k, mbp[i][j][k].sky);

							if(mbp[i][j][k].biome != null)
							{
								as.setBiome(i, k, mbp[i][j][k].biome);
							}
						}
					}
				}
			}

			U.getService(NMSSVC.class).sendChunkUnload(x, z, p);
			U.getService(NMSSVC.class).sendChunkMap(as, p);

			new S(1)
			{
				@Override
				public void run()
				{
					U.getService(NMSSVC.class).sendChunkMap(as, p);
				}
			};
		}

		catch(Exception e)
		{
			System.out.println("Failed to send chunk packet on MC " + Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")");
			e.printStackTrace();
		}

		return 0;
	}

	public int projectOlder(Player p)
	{
		if(VersionBukkit.wc() || !Settings.USE_LIGHTMAPS)
		{
			return projectOldest(p);
		}

		if(cx == null)
		{
			return projectOldest(p);
		}

		try
		{
			for(int i = 0; i < 16; i++)
			{
				for(int k = 0; k < 16; k++)
				{
					for(int j = 0; j < 256; j++)
					{
						cx.setSkyLight(i, j, k, (byte) 15);
						cx.setBlockLight(i, j, k, (byte) 0);
						cx.setBiome(i, k, cx.getChunk().getWorld().getBiome((cx.getX() * 16) + i, (cx.getZ() * 16) + k));

						if(mbi[i][j][k] != null)
						{
							cx.set(i, j, k, new MaterialBlock(mbi[i][j][k]));
						}

						if(mbp[i][j][k] != null)
						{
							cx.setBlockLight(i, j, k, mbp[i][j][k].block);
							cx.setSkyLight(i, j, k, mbp[i][j][k].sky);

							if(mbp[i][j][k].biome != null)
							{
								cx.setBiome(i, k, mbp[i][j][k].biome);
							}
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

	public int projectOldest(Player p)
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
