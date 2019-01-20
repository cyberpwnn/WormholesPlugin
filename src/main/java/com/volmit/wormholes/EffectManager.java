package com.volmit.wormholes;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import com.volmit.wormholes.util.lang.GList;
import com.volmit.wormholes.util.lang.MSound;
import com.volmit.wormholes.util.lang.ParticleEffect;

public class EffectManager implements Listener
{
	public EffectManager()
	{

	}

	public void playPortalBlockPlaced(Block block)
	{
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.FRAME_FILL.bukkitSound(), 1.2f, 1.1f + ((float) (Math.random() * 0.2)));
	}

	public void playPortalBlockDestroyed(Block block)
	{
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.EYE_DEATH.bukkitSound(), 0.7f, 1.46f + ((float) (Math.random() * 0.2)));
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.GLASS.bukkitSound(), 0.7f, 1.55f + ((float) (Math.random() * 0.2)));
	}

	public void playPortalOpen(Set<Block> blocks)
	{
		Block block = new GList<Block>(blocks).pickRandom();
		block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5), MSound.FRAME_SPAWN.bukkitSound(), 2.5f, 1.0f + ((float) (Math.random() * 0.1)));
	}

	public void playPortalOpening(int size, Block cursor)
	{
		for(int i = 0; i < 32; i++)
		{
			Vector up = new Vector(0, 1, 0);
			up.add(Vector.getRandom().subtract(Vector.getRandom()).clone().multiply(0.225));
			up.multiply(4.625);
			ParticleEffect.BLOCK_DUST.display(new ParticleEffect.BlockData(Material.PORTAL, (byte) 0), up.multiply(0.115), 1, cursor.getLocation().clone().add(0.5, 0.5, 0.5), 22);
			ParticleEffect.PORTAL.display(up.multiply(5.2), 1, cursor.getLocation().clone().add(0.5, 0.5, 0.5), 32);
		}

		cursor.getWorld().playSound(cursor.getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_CHORUS_FLOWER_GROW, 2.2f, 0.01f + ((float) size / 12F) + ((float) (Math.random() * 0.01)));
		cursor.getWorld().playSound(cursor.getLocation().clone().add(0.5, 0.5, 0.5), Sound.BLOCK_CHORUS_FLOWER_DEATH, 2.2f, 0.01f + ((float) size / 22F) + ((float) (Math.random() * 0.01)));
	}
}
