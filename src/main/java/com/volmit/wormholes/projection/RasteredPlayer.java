package com.volmit.wormholes.projection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.volmit.volume.bukkit.nms.adapter.AbstractChunk;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.chunk.NMSChunk10;
import com.volmit.wormholes.chunk.NMSChunk11;
import com.volmit.wormholes.chunk.NMSChunk12;
import com.volmit.wormholes.chunk.NMSChunk19;
import com.volmit.wormholes.chunk.VirtualChunk;
import com.volmit.wormholes.exception.NMSChunkFailureException;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.VersionBukkit;

public class RasteredPlayer
{
	private GMap<Location, MaterialBlock> queuedLayer;
	private GMap<Location, BlockProperties> queuedMetaLayer;
	private GMap<Location, MaterialBlock> ghostLayer;
	private GMap<Location, BlockProperties> ghostMetaLayer;
	private GMap<Chunk, VirtualChunk> virtualChunks;
	private GMap<Chunk, AbstractChunk> virtualGUCChunks;
	private GMap<Chunk, Long> mapped;
	private Player p;
	private Queue<Runnable> q;

	public RasteredPlayer(Player p)
	{
		this.p = p;
		queuedLayer = new GMap<Location, MaterialBlock>();
		queuedMetaLayer = new GMap<Location, BlockProperties>();
		ghostLayer = new GMap<Location, MaterialBlock>();
		ghostMetaLayer = new GMap<Location, BlockProperties>();
		q = new ConcurrentLinkedQueue<Runnable>();
		virtualChunks = new GMap<Chunk, VirtualChunk>();
		virtualGUCChunks = new GMap<Chunk, AbstractChunk>();
		mapped = new GMap<Chunk, Long>();
	}

	public void trickLight()
	{
		for(Chunk i : virtualChunks.keySet())
		{
			virtualChunks.get(i).trickLight(p);
		}
	}

	public void wc(Location c)
	{
		if(virtualChunks.containsKey(c.getChunk()))
		{
			virtualChunks.get(c.getChunk()).set(c.getBlockX() & 15, c.getBlockY(), c.getBlockZ() & 15, new MaterialBlock(c));
			virtualGUCChunks.get(c.getChunk()).set(c.getBlockX() & 15, c.getBlockY(), c.getBlockZ() & 15, new com.volmit.volume.bukkit.util.world.MaterialBlock(c));
		}
	}

	public void queue(Location l, BlockProperties bp)
	{
		q.add(new Runnable()
		{
			@Override
			public void run()
			{
				queuedMetaLayer.put(l, bp);
			}
		});
	}

	public void queue(Location l, MaterialBlock mb)
	{
		q.add(new Runnable()
		{
			@Override
			public void run()
			{
				queuedLayer.put(l, mb);
			}
		});
	}

	public void flush()
	{
		try
		{
			flushQueue();

			for(Location i : queuedLayer.k())
			{
				try
				{
					MaterialBlock actual = new MaterialBlock(i);
					BlockProperties actualbp = queuedMetaLayer.get(i);

					try
					{
						if(queuedLayer.containsKey(i) && actual != null)
						{
							if(ghostLayer.containsKey(i) && ghostLayer.get(i).equals(queuedLayer.get(i)))
							{
								queuedLayer.remove(i);
								continue;
							}

							ghostLayer.put(i, queuedLayer.get(i));
						}

						if(ghostMetaLayer.containsKey(i) && actualbp != null)
						{
							if(ghostMetaLayer.containsKey(i) && ghostMetaLayer.get(i).equals(queuedMetaLayer.get(i)))
							{
								queuedMetaLayer.remove(i);
								continue;
							}

							ghostMetaLayer.put(i, queuedMetaLayer.get(i));
						}
					}

					catch(Exception e)
					{
						continue;
					}
				}

				catch(Exception e)
				{
					continue;
				}
			}

			if(queuedLayer.isEmpty())
			{
				return;
			}

			try
			{
				prepareChunks();
			}

			catch(NMSChunkFailureException e)
			{
				if(Settings.USE_LIGHTMAPS)
				{
					System.out.println("WAS USING LMAPS BUT FAILED");
					Settings.USE_LIGHTMAPS = false;
					prepareChunks();
				}
			}
		}

		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	private void flushQueue()
	{
		while(!q.isEmpty())
		{
			q.poll().run();
		}
	}

	private int prepareChunks() throws NMSChunkFailureException
	{
		GMap<Chunk, RasteredChunk> preparedChunks = new GMap<Chunk, RasteredChunk>();

		for(Location i : queuedLayer.k())
		{
			Chunk c = i.getChunk();
			VirtualChunk cx = null;
			if(!VersionBukkit.wc() && Settings.USE_LIGHTMAPS && !virtualChunks.containsKey(c))
			{
				if(VersionBukkit.get().equals(VersionBukkit.V112))
				{
					cx = new NMSChunk12(c);
				}

				else if(VersionBukkit.get().equals(VersionBukkit.V111))
				{
					cx = new NMSChunk11(c);
				}

				else if(VersionBukkit.get().equals(VersionBukkit.V11))
				{
					cx = new NMSChunk10(c);
				}

				else if(VersionBukkit.get().equals(VersionBukkit.V9))
				{
					cx = new NMSChunk19(c);
				}

				if(cx != null)
				{
					virtualChunks.put(c, cx);
				}
			}

			if(!virtualGUCChunks.containsKey(c))
			{
				virtualGUCChunks.put(c, new AbstractChunk(c));
			}

			if(!preparedChunks.containsKey(c))
			{
				preparedChunks.put(c, new RasteredChunk(c.getX(), c.getZ(), c.getWorld(), virtualChunks.get(c), virtualGUCChunks.get(c)));
			}

			preparedChunks.get(c).put(i.getBlockX(), i.getBlockY(), i.getBlockZ(), queuedLayer.get(i));
			queuedLayer.remove(i);

			if(queuedMetaLayer.containsKey(i))
			{
				preparedChunks.get(c).put(i.getBlockX(), i.getBlockY(), i.getBlockZ(), queuedMetaLayer.get(i));
				queuedMetaLayer.remove(i);
			}
		}

		int k = 0;

		for(Chunk i : preparedChunks.k())
		{
			k++;

			if(!mapped.containsKey(i))
			{
				mapped.put(i, M.ms());
				preparedChunks.get(i).projectNew(p);
			}

			else
			{
				preparedChunks.get(i).projectNew(p);
			}
		}

		return k;
	}

	public void dequeueAll()
	{
		queuedLayer.clear();
		queuedMetaLayer.clear();

		for(Location i : ghostLayer.k())
		{
			queue(i, new MaterialBlock(i));
		}

		for(Location i : ghostMetaLayer.k())
		{
			queue(i, ghostMetaLayer.get(i));
		}

		mapped.clear();
	}

	public boolean isQueued(Location l)
	{
		return queuedLayer.containsKey(l) && !queuedLayer.get(l).equals(new MaterialBlock(l));
	}

	public int queueSize()
	{
		return queuedLayer.size();
	}

	public GMap<Location, MaterialBlock> getQueuedLayer()
	{
		return queuedLayer;
	}

	public void setQueuedLayer(GMap<Location, MaterialBlock> queuedLayer)
	{
		this.queuedLayer = queuedLayer;
	}

	public GMap<Location, BlockProperties> getQueuedMetaLayer()
	{
		return queuedMetaLayer;
	}

	public void setQueuedMetaLayer(GMap<Location, BlockProperties> queuedMetaLayer)
	{
		this.queuedMetaLayer = queuedMetaLayer;
	}

	public GMap<Location, MaterialBlock> getGhostLayer()
	{
		return ghostLayer;
	}

	public void setGhostLayer(GMap<Location, MaterialBlock> ghostLayer)
	{
		this.ghostLayer = ghostLayer;
	}

	public GMap<Location, BlockProperties> getGhostMetaLayer()
	{
		return ghostMetaLayer;
	}

	public void setGhostMetaLayer(GMap<Location, BlockProperties> ghostMetaLayer)
	{
		this.ghostMetaLayer = ghostMetaLayer;
	}

	public GMap<Chunk, VirtualChunk> getVirtualChunks()
	{
		return virtualChunks;
	}

	public void setVirtualChunks(GMap<Chunk, VirtualChunk> virtualChunks)
	{
		this.virtualChunks = virtualChunks;
	}

	public GMap<Chunk, AbstractChunk> getVirtualGUCChunks()
	{
		return virtualGUCChunks;
	}

	public void setVirtualGUCChunks(GMap<Chunk, AbstractChunk> virtualGUCChunks)
	{
		this.virtualGUCChunks = virtualGUCChunks;
	}

	public GMap<Chunk, Long> getMapped()
	{
		return mapped;
	}

	public void setMapped(GMap<Chunk, Long> mapped)
	{
		this.mapped = mapped;
	}

	public Player getP()
	{
		return p;
	}

	public void setP(Player p)
	{
		this.p = p;
	}

	public Queue<Runnable> getQ()
	{
		return q;
	}

	public void setQ(Queue<Runnable> q)
	{
		this.q = q;
	}
}
