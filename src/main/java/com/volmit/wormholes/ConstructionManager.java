package com.volmit.wormholes;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.wormholes.util.lang.ParticleEffect;
import com.volmit.wormholes.util.lang.S;

public class ConstructionManager implements Listener
{
	public ConstructionManager()
	{

	}

	public void constructPortal(Player player, Set<Block> blocks)
	{
		new S(35)
		{
			@Override
			public void run()
			{
				Wormholes.effectManager.playPortalOpen(blocks);
				for(Block i : blocks)
				{
					ParticleEffect.EXPLOSION_HUGE.display(0.6f, 1, i.getLocation(), 32);
				}
			}
		};
	}
}