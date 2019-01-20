package com.volmit.wormholes;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Wormholes extends JavaPlugin
{
	public static Wormholes instance;

	public static void registerListener(Listener l)
	{
		Bukkit.getPluginManager().registerEvents(l, instance);
	}

	public static void unregisterListener(Listener l)
	{
		HandlerList.unregisterAll(l);
	}
}
