package com.volmit.wormholes.projection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.volmit.volume.bukkit.VolumePlugin;
import com.volmit.volume.bukkit.task.A;
import com.volmit.volume.lang.collections.FinalLong;
import com.volmit.volume.lang.format.F;
import com.volmit.volume.math.Profiler;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Cuboid.BlockSnapshot;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GBiset;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.VectorMath;

public class ProjectionPlane
{
	private boolean busy;
	private GMap<Vector, MaterialBlock> mapping;
	private GMap<GBiset<Direction, Direction>, GMap<Vector, MaterialBlock>> remapCache;
	private GMap<GBiset<Direction, Direction>, GMap<Vector, Vector>> ormapCache;
	private double progress;

	public ProjectionPlane()
	{
		progress = -1;
		busy = false;
		mapping = new GMap<Vector, MaterialBlock>();
		remapCache = new GMap<GBiset<Direction, Direction>, GMap<Vector, MaterialBlock>>();
		ormapCache = new GMap<GBiset<Direction, Direction>, GMap<Vector, Vector>>();
	}

	public double getProgress()
	{
		return progress;
	}

	public GMap<Vector, MaterialBlock> getMapping()
	{
		return mapping;
	}

	public boolean hasContent()
	{
		return !mapping.isEmpty();
	}

	public Vector ovap(Direction from, Direction to, Vector init)
	{
		return omap(from, to).get(init);
	}

	public void wipe()
	{
		mapping.clear();
		remapCache.clear();
		ormapCache.clear();
	}

	public GMap<Vector, Vector> omap(Direction from, Direction to)
	{
		return ormapCache.get(new GBiset<Direction, Direction>(from, to));
	}

	public GMap<Vector, MaterialBlock> remap(Direction from, Direction to)
	{
		GBiset<Direction, Direction> c = new GBiset<Direction, Direction>(from, to);

		if(!remapCache.containsKey(c))
		{
			GMap<Vector, MaterialBlock> map = new GMap<Vector, MaterialBlock>();
			GMap<Vector, Vector> mapv = new GMap<Vector, Vector>();

			for(Vector i : mapping.k())
			{
				Vector b = i.clone();
				Vector k = from.angle(b, to);
				k.setX(k.getBlockX());
				k.setY(k.getBlockY());
				k.setZ(k.getBlockZ());
				map.put(k, mapping.get(i));
				mapv.put(i, k);
			}

			remapCache.put(c, map);
			ormapCache.put(c, mapv);
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
		GMap<Vector, MaterialBlock> mapping = this.mapping;
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

	public void blockChange(Vector v, MaterialBlock mb)
	{
		mapping.put(v, mb);
		remapCache.clear();
	}

	public void sample(Location l, int rad, boolean vertical, UUID tag)
	{
		if(busy)
		{
			return;
		}

		String tid = tag.toString().split("-")[1];
		VolumePlugin.vpi.getLogger().info("Starting Projection Mapper for " + tid);
		Profiler px = new Profiler();
		px.begin();
		busy = true;
		remapCache.clear();
		FinalLong last = new FinalLong(M.ms());

		Cuboid cx = new Cuboid(l);

		for(Direction i : Direction.udnews())
		{
			cx = cx.e(i, rad);
		}

		Cuboid c = cx;
		Iterator<BlockSnapshot> it = c.iteratorSnapshot();

		new A()
		{
			@Override
			public void run()
			{
				int of = c.volume();
				int did = 0;
				int add = 0;

				while(it.hasNext())
				{
					if(M.ms() - last.get() > 1000)
					{
						last.set(M.ms());
						VolumePlugin.vpi.getLogger().info("Mapping Projection " + tid + " " + F.pc((double) did / (double) of) + " Efficnency: " + F.pc((double) add / (double) of));
					}

					BlockSnapshot i = it.next();
					boolean f = false;

					for(BlockSnapshot j : i.nearby)
					{
						if(j.mb.getMaterial().isTransparent())
						{
							f = true;
							break;
						}
					}

					if(f)
					{
						add++;
						BlockProperties bp = new BlockProperties();
						bp.sky = (byte) i.skylight;
						bp.block = (byte) i.blocklight;
						bp.biome = i.biome;
						mapping.put(VectorMath.directionNoNormal(c.getCenter(), i.l).clone().add(new Vector(0.5, 0.5, 0.5)), i.mb);
					}

					did++;
					progress = (double) did / (double) of;
				}

				px.end();

				VolumePlugin.vpi.getLogger().info("Finished Projection Map " + tid + " in " + F.time(px.getMilliseconds(), 0) + " with an efficiency of " + F.pc((double) add / (double) of));
				remapCache.clear();
				ormapCache.clear();
				busy = false;
			}
		};
	}

	public GMap<GBiset<Direction, Direction>, GMap<Vector, MaterialBlock>> getRemapCache()
	{
		return remapCache;
	}

	public GMap<GBiset<Direction, Direction>, GMap<Vector, Vector>> getOrmapCache()
	{
		return ormapCache;
	}

	public boolean isBusy()
	{
		return busy;
	}
}
