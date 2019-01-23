package com.volmit.wormholes.chunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.volmit.wormholes.exception.NMSChunkFailureException;
import com.volmit.wormholes.util.GList;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NibbleArray;

public class NMSChunk18 extends NMSChunk implements VirtualChunk
{
	private net.minecraft.server.v1_8_R3.Chunk nmsChunk;

	public NMSChunk18(Chunk bukkitChunk) throws NMSChunkFailureException
	{
		super(bukkitChunk, "1_8_R3");

		nmsChunk = ((CraftChunk) getChunk()).getHandle();
		pack();
	}

	@Override
	public void pack() throws NMSChunkFailureException
	{
		try
		{
			for(ChunkSection i : nmsChunk.getSections())
			{
				if(i == null)
				{
					continue;
				}

				for(int j = 0; j < 16; j++)
				{
					for(int k = 0; k < 16; k++)
					{
						for(int l = 0; l < 16; l++)
						{

							IBlockData ibd = i.getType(j, k, l);
							int id = Block.getId(ibd.getBlock());
							byte data = (byte) ibd.getBlock().toLegacyData(ibd);
							setSect(i.getYPosition() >> 4, j, k, l, id, data);
							skyLight[i.getYPosition() >> 4] = i.getSkyLightArray().a();
							blockLight[i.getYPosition() >> 4] = i.getEmittedLightArray().a();
						}
					}
				}
			}

			heightMap = Arrays.copyOf(nmsChunk.heightMap, nmsChunk.heightMap.length);
		}

		catch(Exception e)
		{
			throw new NMSChunkFailureException("Failed to pack data for " + toString(), e);
		}
	}

	@Override
	public void setSkyLight(int x, int y, int z, int value)
	{
		NibbleArray ni = new NibbleArray(skyLight[getSection(y)]);
		ni.a(x, y & 15, z, value);
	}

	@Override
	public void setBlockLight(int x, int y, int z, int value)
	{
		NibbleArray ni = new NibbleArray(blockLight[getSection(y)]);
		ni.a(x, y & 15, z, value);
	}

	@Override
	public void send(Player p)
	{
		PacketContainer container = new PacketContainer(PacketType.Play.Server.MAP_CHUNK);

		try
		{
			write(container);
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, container);
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void write(PacketContainer packet) throws IOException
	{
		StructureModifier<Integer> ints = packet.getIntegers();
		StructureModifier<byte[]> byteArray = packet.getByteArrays();
		StructureModifier<Boolean> bools = packet.getBooleans();

		ints.write(0, getX());
		ints.write(1, getZ());
		bools.write(0, false);
		ints.write(2, getBitMask());

		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		NMOutputStream nm = new NMOutputStream(boas);
		int[][] data = blockData;

		for(int i = 0; i < data.length; i++)
		{
			int[] section = data[i];
			if(!modifiedSections[i])
			{
				continue;
			}

			int num = MathHelper.d(new GList<IBlockData>(Block.d.iterator()).size());
			nm.write(num);
			nm.writeVarInt(0);
			DataBits18 bits = new DataBits18(num, 4096);
			bits.a(0, 0);

			for(int j = 0; j < 4096; j++)
			{
				int id = section[j];

				if(id != 0 && id <= 8191)
				{
					bits.a(j, id);
				}
			}

			nm.writeVarInt(bits.a().length);

			for(long j : bits.a())
			{
				nm.writeLong(j);
			}

			nm.write(blockLight[i]);
			nm.write(skyLight[i]);
		}

		byteArray.write(0, boas.toByteArray());
		nm.close();
	}

	@Override
	public void setBlockLight(int i, int j, int k, byte block)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setSkyLight(int i, int j, int k, byte sky)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setBiome(int i, int k, Biome biome)
	{
		// TODO Auto-generated method stub

	}
}
