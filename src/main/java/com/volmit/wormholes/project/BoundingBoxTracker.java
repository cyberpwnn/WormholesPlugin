package com.volmit.wormholes.project;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.volmit.wormholes.util.AxisAlignedBB;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GSet;

public class BoundingBoxTracker implements IBoundingBoxTracker<Player>
{
	private GSet<Player> inside;
	private GSet<Player> entering;
	private GSet<Player> exiting;
	private AxisAlignedBB bb;
	private World world;

	public BoundingBoxTracker(AxisAlignedBB bb, World world)
	{
		this.world = world;
		this.bb = bb;
		inside = new GSet<>();
		entering = new GSet<>();
		exiting = new GSet<>();
	}

	@Override
	public GSet<Player> getInside()
	{
		return inside;
	}

	@Override
	public GSet<Player> getEntering()
	{
		return entering;
	}

	@Override
	public GSet<Player> getExiting()
	{
		return exiting;
	}

	@Override
	public GList<Player> get()
	{
		GList<Player> t = new GList<>();
		for(Entity i : bb.getEntities(world))
		{
			if(i instanceof Player)
			{
				t.add((Player) i);
			}
		}

		return t;
	}

	@Override
	public void update()
	{
		exiting.clear();
		entering.clear();

		for(Player i : get())
		{
			if(bb.contains(i.getLocation()) && !inside.contains(i))
			{
				entering.add(i);
				inside.add(i);
			}

			if(!bb.contains(i.getLocation()) && inside.contains(i))
			{
				exiting.add(i);
				inside.remove(i);
			}
		}
	}
}
