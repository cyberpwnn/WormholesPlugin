package com.volmit.wormholes.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class SubCommand
{
	private String sub;
	private GList<String> osub;
	private String description;
	
	public SubCommand(String description, String sub, String... osub)
	{
		this.description = description;
		this.sub = sub;
		this.osub = new GList<String>(osub);
	}
	
	public abstract void cs(CommandSender p, String[] args);
	
	public abstract void cp(Player p, String[] args);
	
	public String getSub()
	{
		return sub;
	}
	
	public GList<String> getOsub()
	{
		return osub;
	}
	
	public String getDescription()
	{
		return description;
	}
}
