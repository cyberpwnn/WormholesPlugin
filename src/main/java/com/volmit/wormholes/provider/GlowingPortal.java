package com.volmit.wormholes.provider;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.volume.bukkit.U;
import com.volmit.volume.bukkit.nms.NMSSVC;
import com.volmit.volume.bukkit.util.text.C;
import com.volmit.volume.bukkit.util.world.MaterialBlock;
import com.volmit.volume.lang.collections.GList;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Direction;

public class GlowingPortal implements Listener
{
	private GList<GlowingBlock> entities;
	private Material m;
	private Player p;
	private boolean dead;
	private ChatColor oldcolor;
	private ChatColor color;

	public GlowingPortal(Player p, Material m)
	{
		oldcolor = ChatColor.GOLD;
		color = ChatColor.GOLD;
		dead = false;
		this.m = m;
		this.p = p;
		entities = new GList<GlowingBlock>();
	}

	public void move(Cuboid c)
	{
		GList<Block> spawn = new GList<Block>();
		GList<GlowingBlock> blocks = new GList<GlowingBlock>();

		for(Direction i : Direction.udnews())
		{
			if(c.depth(i.getAxis()) > 1)
			{
				spawn.addAll(new GList<Block>(c.getFace(i.f()).iterator()));
			}
		}

		for(Block b : spawn)
		{
			Location i = b.getLocation().clone().add(0.5, -0.1, 0.5);
			GlowingBlock fb = pullEntity(p, m, c);
			fb.setPosition(i);
			blocks.add(fb);
			fb.update();
		}

		if(!entities.isEmpty())
		{
			despawn();
		}

		entities.addAll(blocks);

		if(!color.equals(oldcolor))
		{
			for(GlowingBlock i : entities)
			{
				U.getService(NMSSVC.class).sendGlowingColorMetaEntity(p, i.getUid(), C.values()[color.ordinal()]);
			}

			oldcolor = color;
		}
	}

	public void setDone()
	{
		color = ChatColor.GREEN;
	}

	@SuppressWarnings("deprecation")
	public GlowingBlock pullEntity(Player p, Material m, Cuboid c)
	{
		if(!entities.isEmpty())
		{
			return entities.pop();
		}

		GlowingBlock fb = new GlowingBlock(p, c.getCenter(), new MaterialBlock(m), ChatColor.GOLD);
		fb.sendSpawn();

		return fb;
	}

	public void despawn()
	{
		for(GlowingBlock i : entities)
		{
			i.sendDestroy();
		}

		entities.clear();
	}

	public void die()
	{
		dead = true;
	}
}

