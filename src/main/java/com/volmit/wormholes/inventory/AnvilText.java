package com.volmit.wormholes.inventory;

import java.util.function.BiFunction;

import org.bukkit.entity.Player;

import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.util.RString;

public class AnvilText
{
	public static void getText(Player p, String def, RString s)
	{
		try
		{
			new AnvilGUI(Wormholes.instance, p, def, new BiFunction<Player, String, String>()
			{
				@Override
				public String apply(Player t, String u)
				{
					s.onComplete(u);
					t.closeInventory();

					return "";
				}
			});
		}

		catch(Exception e)
		{

		}
	}
}