package com.volmit.wormholes.config;

import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import com.volmit.wormholes.Info;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.GMap;

public class Permissable
{
	private static GMap<DyeColor, String> permdye = null;
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
		sdye();
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
	
	private void sdye()
	{
		if(permdye == null)
		{
			permdye = new GMap<DyeColor, String>();
			permdye.put(DyeColor.WHITE, "wormholes.use.white");
			permdye.put(DyeColor.ORANGE, "wormholes.use.orange");
			permdye.put(DyeColor.MAGENTA, "wormholes.use.magenta");
			permdye.put(DyeColor.LIGHT_BLUE, "wormholes.use.lightblue");
			permdye.put(DyeColor.YELLOW, "wormholes.use.yellow");
			permdye.put(DyeColor.LIME, "wormholes.use.lime");
			permdye.put(DyeColor.PINK, "wormholes.use.pink");
			permdye.put(DyeColor.GRAY, "wormholes.use.gray");
			permdye.put(DyeColor.SILVER, "wormholes.use.lightgray");
			permdye.put(DyeColor.CYAN, "wormholes.use.cyan");
			permdye.put(DyeColor.PURPLE, "wormholes.use.purple");
			permdye.put(DyeColor.BLUE, "wormholes.use.blue");
			permdye.put(DyeColor.BROWN, "wormholes.use.brown");
			permdye.put(DyeColor.GREEN, "wormholes.use.green");
			permdye.put(DyeColor.RED, "wormholes.use.red");
			permdye.put(DyeColor.BLACK, "wormholes.use.black");
		}
	}
	
	public boolean canUse(DyeColor c)
	{
		if(has(permdye.get(c)))
		{
			DB.d(this, "has perm for " + permdye.get(c));
		}
		
		return has(permdye.get(c));
	}
	
	public boolean canUse(PortalKey key)
	{
		return canUse(key.getU()) && canUse(key.getD()) && canUse(key.getL()) && canUse(key.getR());
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
	
	public boolean canUse(LocalPortal p)
	{
		return canUse && canUse(p.getKey());
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
