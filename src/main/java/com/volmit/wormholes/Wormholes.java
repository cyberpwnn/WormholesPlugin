package com.volmit.wormholes;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.volmit.wormholes.util.Direction;

public class Wormholes extends JavaPlugin implements Listener
{
	public static Wormholes instance;
	public static TraversableManager traversableManager;
	public static BlockManager blockManager;
	public static EffectManager effectManager;
	public static ConstructionManager constructionManager;
	public static PortalManager portalManager;

	@Override
	public void onEnable()
	{
		Direction.calculatePermutations();
		registerListener(instance = this);
		registerListener(blockManager = new BlockManager());
		registerListener(effectManager = new EffectManager());
		registerListener(constructionManager = new ConstructionManager());
		registerListener(portalManager = new PortalManager());
		registerListener(traversableManager = new TraversableManager());
	}

	public static void registerListener(Listener l)
	{
		Bukkit.getPluginManager().registerEvents(l, instance);
	}

	public static void unregisterListener(Listener l)
	{
		HandlerList.unregisterAll(l);
	}
}
