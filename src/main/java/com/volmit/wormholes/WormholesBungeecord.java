package com.volmit.wormholes;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class WormholesBungeecord extends Plugin implements Listener
{
	@Override
	public void onEnable()
	{
		getLogger().warning("-----------------------------------------------------------------------");
		getLogger().warning("Wormholes is designed to run on each server, not on bungeecord plugins.");
		getLogger().warning("Please install wormholes on servers connected to a bungee network.");
		getLogger().warning("-----------------------------------------------------------------------");
		getProxy().getPluginManager().registerListener(this, this);
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("Stopping Wormholes (MUTEX)");
	}
}
