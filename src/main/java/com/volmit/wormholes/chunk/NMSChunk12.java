package com.volmit.wormholes.chunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.volmit.wormholes.exception.NMSChunkFailureException;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.DataBits;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.MathHelper;
import net.minecraft.server.v1_12_R1.NibbleArray;

public class NMSChunk12 extends NMSChunk implements VirtualChunk
{
	private net.minecraft.server.v1_12_R1.Chunk nmsChunk;

	public NMSChunk12(Chunk bukkitChunk) throws NMSChunkFailureException
	{
		super(bukkitChunk, "1_12_R1");

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
							IBlockData ibd = i.getBlocks().a(j, k, l);
							int id = Block.getId(ibd.getBlock());
							byte data = (byte) ibd.getBlock().toLegacyData(ibd);
							setSect(i.getYPosition() >> 4, j, k, l, id, data);
							skyLight[i.getYPosition() >> 4] = i.getSkyLightArray().asBytes();
							blockLight[i.getYPosition() >> 4] = i.getEmittedLightArray().asBytes();
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

			int num = MathHelper.d(Block.REGISTRY_ID.a());
			nm.write(num);
			nm.writeVarInt(0);
			DataBits bits = new DataBits(num, 4096);
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
		packet.getModifier().withType(Collection.class, BukkitConverters.getListConverter(MinecraftReflection.getNBTBaseClass(), BukkitConverters.getNbtConverter())).write(0, new ArrayList<>());
		nm.close();
	}

	@Override
	public void setBlockLight(int i, int j, int k, byte block)
	{
		setBlockLight(i, j, k, (int) block);
	}

	@Override
	public void setSkyLight(int i, int j, int k, byte sky)
	{
		setSkyLight(i, j, k, (int) sky);
	}

	@Override
	public void setBiome(int i, int k, Biome biome)
	{

	}
}
