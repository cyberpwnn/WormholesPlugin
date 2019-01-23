package com.volmit.wormholes;

import org.bukkit.Sound;

public class SoundMGR
{
	public Sound soundForPortalCreateUnlinked()
	{
		try
		{
			return Sound.valueOf("ENTITY_ENDEREYE_DEATH");
		}

		catch(Exception e)
		{

		}

		return Sound.ENTITY_LIGHTNING_THUNDER;
	}
}
