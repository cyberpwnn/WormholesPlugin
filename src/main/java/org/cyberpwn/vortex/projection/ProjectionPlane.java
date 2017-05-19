package org.cyberpwn.vortex.projection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import wraith.Cuboid;
import wraith.Direction;
import wraith.GBiset;
import wraith.GList;
import wraith.GMap;
import wraith.MaterialBlock;
import wraith.VectorMath;
import wraith.W;

public class ProjectionPlane
{
	private GMap<Vector, MaterialBlock> mapping;
	private GMap<GBiset<Direction, Direction>, GMap<Vector, MaterialBlock>> remapCache;
	
	public ProjectionPlane()
	{
		mapping = new GMap<Vector, MaterialBlock>();
		remapCache = new GMap<GBiset<Direction, Direction>, GMap<Vector, MaterialBlock>>();
	}
	
	public GMap<Vector, MaterialBlock> getMapping()
	{
		return mapping;
	}
	
	public boolean hasContent()
	{
		return !mapping.isEmpty();
	}
	
	public GMap<Vector, MaterialBlock> remap(Direction from, Direction to)
	{
		GBiset<Direction, Direction> c = new GBiset<Direction, Direction>(from, to);
		
		if(!remapCache.containsKey(c))
		{
			GMap<Vector, MaterialBlock> map = new GMap<Vector, MaterialBlock>();
			
			for(Vector i : mapping.k())
			{
				Vector b = i.clone().add(new Vector(0.5, 0.5, 0.5));
				map.put(from.angle(b, to), mapping.get(i));
			}
			
			remapCache.put(c, map);
		}
		
		return remapCache.get(c);
	}
	
	public void addSuperCompressed(byte[] data) throws IOException
	{
		ByteArrayInputStream bois = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(bois);
		int size = in.readInt();
		
		for(int i = 0; i < size; i++)
		{
			long d = in.readLong();
			MBC mbc = new MBC(d);
			Vector v = mbc.getV();
			MaterialBlock mb = mbc.getMb();
			mapping.put(v, mb);
		}
		
		in.close();
		remapCache.clear();
	}
	
	public GList<Byte[]> getSuperCompressedByteCull(int maxBytesPerPackage) throws IOException
	{
		return getSuperCompressed(maxBytesPerPackage / 8);
	}
	
	public GList<Byte[]> getSuperCompressed(int maxSize) throws IOException
	{
		GList<Byte[]> bytes = new GList<Byte[]>();
		GMap<Vector, MaterialBlock> mapping = this.mapping.copy();
		GList<Vector> vectors = mapping.k();
		
		while(!vectors.isEmpty())
		{
			int size = vectors.size() < maxSize ? vectors.size() : maxSize;
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(boas);
			out.writeInt(size);
			
			for(int i = 0; i < size; i++)
			{
				Vector v = vectors.pop();
				MaterialBlock mb = mapping.get(v);
				long compressed = new MBC(mb, v).toLong();
				out.writeLong(compressed);
			}
			
			out.close();
			Byte[] compress = ArrayUtils.toObject(boas.toByteArray());
			bytes.add(compress);
		}
		
		return bytes;
	}
	
	@SuppressWarnings("deprecation")
	public void sample(Location l, int rad)
	{
		remapCache.clear();
		Cuboid c = new Cuboid(l);
		
		for(Direction i : Direction.udnews())
		{
			c = c.e(i, rad);
		}
		
		for(Block i : new GList<Block>(c.iterator()))
		{
			if(i.getType().isTransparent() && !i.getType().equals(Material.AIR))
			{
				mapping.put(VectorMath.directionNoNormal(c.getCenter(), i.getLocation()), new MaterialBlock(i.getType(), i.getData()));
				continue;
			}
			
			for(Block j : W.blockFaces(i))
			{
				if(j.getType().isTransparent())
				{
					mapping.put(VectorMath.directionNoNormal(c.getCenter(), i.getLocation()), new MaterialBlock(i.getType(), i.getData()));
					break;
				}
			}
		}
	}
}
