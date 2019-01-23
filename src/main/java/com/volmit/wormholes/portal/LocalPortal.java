package com.volmit.wormholes.portal;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.volmit.catalyst.api.NMP;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.geometry.Raycast;
import com.volmit.wormholes.util.lang.C;
import com.volmit.wormholes.util.lang.FinalBoolean;
import com.volmit.wormholes.util.lang.M;
import com.volmit.wormholes.util.lang.MSound;
import com.volmit.wormholes.util.lang.ParticleEffect;
import com.volmit.wormholes.util.lang.PhantomSpinner;

public class LocalPortal extends Portal implements ILocalPortal, IProgressivePortal, IFXPortal
{
	private final PhantomSpinner spinner;
	private final PortalStructure structure;
	private final PortalType type;
	private boolean open;
	private boolean progressing;
	private String progress;

	public LocalPortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id);
		spinner = new PhantomSpinner(C.YELLOW, C.GOLD, C.RED);
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
				for(int i = 0; i < 4; i++)
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
			case AMBIENT_INSPECTING:
				if(M.r(0.325))
				{
					for(Location i : getStructure().getCorners())
					{
						ParticleEffect.FLAME.display(0f, 1, i, 32);
					}
				}

				ParticleEffect.ENCHANTMENT_TABLE.display(0f, 1, getStructure().randomLocation(), 32);

			case AMBIENT_DEBUG:

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

	@Override
	public void onLooking(Player p, boolean holdingWand)
	{
		if(holdingWand)
		{
			playEffect(PortalEffect.AMBIENT_INSPECTING);

			if(isShowingProgress())
			{
				NMP.MESSAGE.title(p, "", C.GRAY + "" + C.BOLD + "" + getName() + " " + spinner.toString() + C.RESET + C.GRAY + progress, 0, 2, 3);
			}

			else
			{
				NMP.MESSAGE.title(p, "", C.GRAY + "" + C.BOLD + "" + getName(), 0, 2, 3);
			}
		}
	}

	@Override
	public void onWanded(Player p)
	{

	}

	@Override
	public boolean isLookingAt(Player p)
	{
		if(p.getWorld().equals(getStructure().getWorld()))
		{
			if(p.getLocation().distanceSquared(getStructure().getCenter()) < 64)
			{
				FinalBoolean hit = new FinalBoolean(false);

				new Raycast(p.getEyeLocation(), p.getEyeLocation().clone().add(p.getLocation().getDirection().clone().multiply(16)), 0.9)
				{
					@Override
					public boolean shouldContinue(Location l)
					{
						if(getStructure().getArea().contains(l))
						{
							hit.set(true);
							return false;
						}

						return true;
					}
				};

				return hit.get();
			}
		}

		return false;
	}
}
