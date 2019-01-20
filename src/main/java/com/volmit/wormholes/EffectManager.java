package com.volmit.wormholes;

import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import com.volmit.wormholes.util.lang.MSound;

public class EffectManager implements Listener
{
	public EffectManager()
	{

	}

	public void playPortalBlockPlaced(Block block)
	{
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.EYE_DEATH.bukkitSound(), 1f, 0.5f);
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.EYE_DEATH.bukkitSound(), 1f, 1.7f);
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.EYE_DEATH.bukkitSound(), 1f, 1.2f);
	}

	public void playPortalBlockDestroyed(Block block)
	{
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.FRAME_FILL.bukkitSound(), 1f, 0.36f);
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.FRAME_FILL.bukkitSound(), 1f, 0.5f);
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.FRAME_FILL.bukkitSound(), 1f, 0.73f);
	}
}
