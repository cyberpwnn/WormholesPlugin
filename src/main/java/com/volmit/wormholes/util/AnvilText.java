package com.volmit.wormholes.util;

import java.util.function.BiFunction;

import org.bukkit.entity.Player;

import com.volmit.wormholes.Wormholes;

public class AnvilText
{
	public static void getText(Player p, String def, RString s)
	{
		new GSound(MSound.DOOR_OPEN.bukkitSound(), 1f, 1.5f).play(p);
		new GSound(MSound.DOOR_OPEN.bukkitSound(), 1f, 1.8f).play(p);

		try
		{
			new AnvilGUI(Wormholes.instance, p, def, new BiFunction<Player, String, String>()
			{
				@Override
				public String apply(Player t, String u)
				{
					s.onComplete(u);
					t.closeInventory();
					new GSound(MSound.BAT_TAKEOFF.bukkitSound(), 1f, 1.5f).play(t);
					new GSound(MSound.DOOR_CLOSE.bukkitSound(), 1f, 1.8f).play(t);

					return "";
				}
			});
		}

		catch(Exception e)
		{

		}
	}
}
