package com.volmit.wormholes.portal;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.geometry.Frustum4D;
import com.volmit.wormholes.util.lang.M;
import com.volmit.wormholes.util.lang.MSound;
import com.volmit.wormholes.util.lang.ParticleEffect;

public class LocalPortal extends Portal implements ILocalPortal, IProgressivePortal, IFXPortal
{
	private final PortalStructure structure;
	private final PortalType type;
	private boolean open;
	private boolean progressing;
	private String progress;

	public LocalPortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id);
		this.type = type;
		this.structure = structure;
		open = false;
		progressing = false;
		progress = "Idle";
	}

	@Override
	public PortalStructure getStructure()
	{
		return structure;
	}

	@Override
	public PortalType getType()
	{
		return type;
	}

	@Override
	public void update()
	{
		if(isOpen())
		{
			playEffect(PortalEffect.AMBIENT_OPEN);
		}

		else
		{
			playEffect(PortalEffect.AMBIENT_CLOSED);
		}

		if(Settings.DEBUG_RENDERING)
		{
			playEffect(PortalEffect.AMBIENT_DEBUG);
		}
	}

	@Override
	public void close()
	{
		setOpen(false);
	}

	@Override
	public boolean isOpen()
	{
		return open;
	}

	@Override
	public void open()
	{
		setOpen(true);
	}

	@Override
	public void setOpen(boolean open)
	{
		this.open = open;
	}

	@Override
	public void playEffect(PortalEffect effect, Location location)
	{
		switch(effect)
		{
			case AMBIENT_CLOSED:
				for(int i = 0; i < 4; i++)
				{
					ParticleEffect.TOWN_AURA.display(0f, 1, getStructure().randomLocation(), 16);
				}

				break;
			case AMBIENT_OPEN:
				for(int i = 0; i < 12; i++)
				{
					ParticleEffect.TOWN_AURA.display(0f, 1, getStructure().randomLocation(), 16);
				}

				if(M.r(0.01))
				{
					getStructure().getCenter().getWorld().playSound(getStructure().getCenter(), Sound.BLOCK_LAVA_AMBIENT, 0.25f, 0.025f);
				}

				if(M.r(0.01))
				{
					getStructure().getCenter().getWorld().playSound(getStructure().getCenter(), MSound.PORTAL.bukkitSound(), 0.25f, 0.025f);
				}

				break;
			case CLOSE:
				break;
			case OPEN:
				getStructure().getCenter().getWorld().playSound(getStructure().getCenter(), MSound.FRAME_SPAWN.bukkitSound(), 2.25f, 0.1f);
				getStructure().getCenter().getWorld().playSound(getStructure().getCenter(), MSound.FRAME_SPAWN.bukkitSound(), 2.25f, 1.6f);
				break;
			case AMBIENT_DEBUG:
				for(Location i : getStructure().getCorners())
				{
					ParticleEffect.FLAME.display(0f, 1, i, 32);
				}

				for(int i = 0; i < getStructure().getArea().volume() / 7; i++)
				{
					// ParticleEffect.BARRIER.display(0f, 1,
					// getStructure().randomLocation().getBlock().getLocation().clone().add(0.5,
					// 0.5, 0.5), 32);
				}

				for(Player i : getStructure().getWorld().getPlayers())
				{
					if(i.getLocation().distanceSquared(getStructure().getCenter()) < 16 * 16)
					{
						Frustum4D frustum = new Frustum4D(i.getEyeLocation(), getStructure(), 20);
					}
				}

				break;
			default:
				break;
		}
	}

	@Override
	public void playEffect(PortalEffect effect)
	{
		playEffect(effect, null);
	}

	@Override
	public void showProgress(String text)
	{
		progressing = true;
		progress = text;
	}

	@Override
	public void hideProgress()
	{
		progressing = false;
	}

	@Override
	public boolean isShowingProgress()
	{
		return progressing;
	}

	@Override
	public String getCurrentProgress()
	{
		return progress;
	}
}
