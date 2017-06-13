package com.volmit.wormholes.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.volmit.wormholes.Info;

public class SubGroup
{
	private GList<SubCommand> subCommands;
	private String root;
	
	public SubGroup(String root)
	{
		this.root = root;
		subCommands = new GList<SubCommand>();
	}
	
	public void add(SubCommand sub)
	{
		subCommands.add(sub);
	}
	
	public void showHelp(CommandSender sender)
	{
		sender.sendMessage(Info.hrn("Commands"));
		
		for(SubCommand i : subCommands)
		{
			sendLine(sender, i);
		}
		
		sender.sendMessage(Info.HR);
	}
	
	public void sendLine(CommandSender sender, SubCommand s)
	{
		sender.sendMessage(C.LIGHT_PURPLE + "/" + root + " " + C.WHITE + s.getSub() + " " + C.GRAY + "- " + s.getDescription());
	}
	
	public void hit(CommandSender sender, String[] args)
	{
		GList<String> a = new GList<String>(args);
		
		if(a.isEmpty())
		{
			showHelp(sender);
			return;
		}
		
		String s = a.get(0);
		a.remove(0);
		
		for(SubCommand i : subCommands)
		{
			if(i.getSub().equalsIgnoreCase(s) || i.getOsub().contains(s.toLowerCase()))
			{
				if(sender instanceof Player)
				{
					i.cp((Player) sender, a.toArray(new String[a.size()]));
				}
				
				else
				{
					i.cs(sender, a.toArray(new String[a.size()]));
				}
				
				return;
			}
		}
		
		showHelp(sender);
	}
}
