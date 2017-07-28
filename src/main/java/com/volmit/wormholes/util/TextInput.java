package com.volmit.wormholes.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public abstract class TextInput implements Listener
{
	private Player p;
	private Object[] o;
	
	public TextInput(Player p, Object... o)
	{
		this.p = p;
		this.o = o;
		Wraith.registerListener(this);
	}
	
	@EventHandler
	public void on(AsyncPlayerChatEvent e)
	{
		if(e.getPlayer().equals(e.getPlayer()))
		{
			Wraith.unregisterListener(this);
			String m = e.getMessage();
			
			new TaskLater()
			{
				@Override
				public void run()
				{
					onResponse(p, m, o);
				}
			};
			
			e.setCancelled(true);
		}
	}
	
	public abstract void onResponse(Player p, String response, Object... o);
}
