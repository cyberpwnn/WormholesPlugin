package com.volmit.wormholes;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.wormholes.util.lang.ParticleEffect;

public class ConstructionManager implements Listener
{
	public ConstructionManager()
	{

	}

	public void constructPortal(Player player, Set<Block> blocks)
	{
		Wormholes.effectManager.playPortalOpen(blocks);
		for(Block i : blocks)
		{
			ParticleEffect.PORTAL.display(0.6f, 8, i.getLocation(), 32);
		}
	}
}