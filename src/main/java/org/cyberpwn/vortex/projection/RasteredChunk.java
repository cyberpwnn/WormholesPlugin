package org.cyberpwn.vortex.projection;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.wrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import wraith.GList;
import wraith.MaterialBlock;

public class RasteredChunk
{
	private int x;
	private int z;
	private MultiBlockChangeInfo[][][] mbi;
	private World world;
	
	public RasteredChunk(int x, int z, World world)
	{
		this.world = world;
		this.x = x;
		this.z = z;
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
	
	public void project(Player p)
	{
		WrapperPlayServerMultiBlockChange w = new WrapperPlayServerMultiBlockChange();
		w.setChunk(new ChunkCoordIntPair(x, z));
		GList<MultiBlockChangeInfo> inf = new GList<MultiBlockChangeInfo>();
		
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 256; j++)
			{
				for(int k = 0; k < 16; k++)
				{
					if(mbi[i][j][k] != null)
					{
						inf.add(mbi[i][j][k]);
					}
				}
			}
		}
		
		w.setRecords(inf.toArray(new MultiBlockChangeInfo[inf.size()]));
		
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
}
