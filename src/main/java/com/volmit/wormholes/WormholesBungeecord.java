package com.volmit.wormholes;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class WormholesBungeecord extends Plugin implements Listener
{
	@Override
	public void onEnable()
	{
		getLogger().info("Starting Wormholes (MUTEX)");
		getProxy().getPluginManager().registerListener(this, this);
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("Stopping Wormholes (MUTEX)");
	}
}
