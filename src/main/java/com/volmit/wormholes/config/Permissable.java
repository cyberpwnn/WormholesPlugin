package com.volmit.wormholes.config;

import org.bukkit.command.CommandSender;
import com.volmit.wormholes.Info;

public class Permissable
{
	private boolean canReload;
	private boolean canCreate;
	private boolean canDestroy;
	private boolean canList;
	private boolean canUse;
	private boolean canBuild;
	private boolean canConfigure;
	private boolean canWand;
	private CommandSender p;
	
	public Permissable(CommandSender p)
	{
		this.p = p;
		canReload = has(Info.PERM_RELOAD);
		canCreate = has(Info.PERM_CREATE);
		canDestroy = has(Info.PERM_DESTROY);
		canList = has(Info.PERM_LIST);
		canUse = has(Info.PERM_USE);
		canBuild = has(Info.PERM_BUILD);
		canConfigure = has(Info.PERM_CONFIGURE);
		canWand = has(Info.PERM_WAND);
	}
	
	public boolean has(String... any)
	{
		for(String i : any)
		{
			if(p.hasPermission(i))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean canReload()
	{
		return canReload;
	}
	
	public boolean canCreate()
	{
		return canCreate;
	}
	
	public boolean canDestroy()
	{
		return canDestroy;
	}
	
	public boolean canList()
	{
		return canList;
	}
	
	public boolean canUse()
	{
		return canUse;
	}
	
	public boolean canBuild()
	{
		return canBuild;
	}
	
	public boolean canConfigure()
	{
		return canConfigure;
	}
	
	public boolean canWand()
	{
		return canWand;
	}
	
	public CommandSender getP()
	{
		return p;
	}
}
