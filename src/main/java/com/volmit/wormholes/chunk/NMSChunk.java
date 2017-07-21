package com.volmit.wormholes.chunk;

import java.util.Arrays;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import com.volmit.wormholes.exception.NMSChunkFailureException;
import com.volmit.wormholes.util.MaterialBlock;

public abstract class NMSChunk implements VirtualChunk
{
	protected Chunk bukkitChunk;
	protected int[][] blockData;
	protected byte[][] skyLight;
	protected byte[][] blockLight;
	protected int[] heightMap;
	protected boolean[] modifiedSections;
	protected String version;
	
	public NMSChunk(Chunk bukkitChunk, String version)
	{
		this.bukkitChunk = bukkitChunk;
		this.version = version;
		blockData = new int[16][];
		skyLight = new byte[16][];
		blockLight = new byte[16][];
		heightMap = new int[256];
		modifiedSections = new boolean[16];
		clearChunk();
	}
	
	public abstract void setSkyLight(int x, int y, int z, int value);
	
	public abstract void setBlockLight(int x, int y, int z, int value);
	
	public void clearChunk()
	{
		for(int i = 0; i < blockData.length; i++)
		{
			clearSection(i);
		}
		
		clearModificationMark();
		Arrays.fill(heightMap, 0);
	}
	
	public void clearSection(int sect)
	{
		blockData[sect] = new int[4096];
		skyLight[sect] = new byte[2048];
		blockLight[sect] = new byte[2048];
		Arrays.fill(blockData[sect], 0);
		Arrays.fill(skyLight[sect], (byte) 0);
		Arrays.fill(blockLight[sect], (byte) 0);
	}
	
	public abstract void pack() throws NMSChunkFailureException;
	
	public void markModification(int x, int y, int z)
	{
		modifiedSections[getSection(y)] = true;
	}
	
	public void clearModificationMark()
	{
		Arrays.fill(modifiedSections, false);
	}
	
	public int getCombined(int id, int data)
	{
		return (id << 4) + data;
	}
	
	public int getId(int combined)
	{
		return combined >> 4;
	}
	
	public byte getData(int combined)
	{
		return (byte) (combined & 15);
	}
	
	@Override
	public void set(int x, int y, int z, int id, byte data)
	{
		try
		{
			if(blockData[getSection(y)][getIndex(x, y, z)] == getCombined(0, 0) && getActualHeight(x, z) == y)
			{
				int k = 1;
				
				while(!get(x, y - k, z).getMaterial().equals(Material.AIR) && y - k > 1)
				{
					k++;
				}
				
				setHeight(x, z, y - k);
			}
			
			if(blockData[getSection(y)][getIndex(x, y, z)] != getCombined(0, 0) && getActualHeight(x, z) < y)
			{
				setHeight(x, z, y);
			}
			
			if(getActualHeight(x, z) <= y)
			{
				setSkyLight(x, y, z, 15);
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		blockData[getSection(y)][getIndex(x, y, z)] = getCombined(id, data);
		markModification(x, y, z);
	}
	
	public void setHeight(int x, int z, int h)
	{
		heightMap[z << 4 | x] = h;
	}
	
	public int getActualHeight(int x, int z)
	{
		return heightMap[z << 4 | x];
	}
	
	public void setSect(int sect, int x, int y, int z, int id, byte data)
	{
		if(sect >= blockData.length)
		{
			return;
		}
		
		blockData[sect][getIndexAmod(x, y, z)] = getCombined(id, data);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void set(int x, int y, int z, MaterialBlock m)
	{
		set(x, y, z, m.getMaterial().getId(), m.getData());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public MaterialBlock get(int x, int y, int z)
	{
		return new MaterialBlock(Material.getMaterial(getId(x, y, z)), getData(x, y, z));
	}
	
	@Override
	public int getId(int x, int y, int z)
	{
		try
		{
			if(blockData[getSection(y)] == null)
			{
				return 0;
			}
			
			return getId(blockData[getSection(y)][getIndex(x, y, z)]);
		}
		
		catch(Exception e)
		{
			return 0;
		}
	}
	
	@Override
	public byte getData(int x, int y, int z)
	{
		if(blockData[getSection(y)] == null)
		{
			return 0;
		}
		
		return getData(blockData[getSection(y)][getIndex(x, y, z)]);
	}
	
	public int getIndex(int x, int y, int z)
	{
		return ((y & 15) << 8) | (z << 4) | (x);
	}
	
	public int getIndexAmod(int x, int y, int z)
	{
		return (y << 8) | (z << 4) | (x);
	}
	
	public int getBitMask()
	{
		int bitMask = 0;
		
		for(int section = 0; section < modifiedSections.length; section++)
		{
			if(modifiedSections[section])
			{
				bitMask += 1 << section;
			}
		}
		
		return bitMask;
	}
	
	public int getSection(int y)
	{
		return y >> 4;
	}
	
	@Override
	public int getX()
	{
		return getChunk().getX();
	}
	
	@Override
	public int getZ()
	{
		return getChunk().getZ();
	}
	
	@Override
	public World getWorld()
	{
		return getChunk().getWorld();
	}
	
	@Override
	public Chunk getChunk()
	{
		return bukkitChunk;
	}
	
	@Override
	public String toString()
	{
		return "NMSC::" + version + "::" + bukkitChunk.getWorld().getName() + "::" + bukkitChunk.getX() + "," + bukkitChunk.getZ();
	}
}
