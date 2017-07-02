package com.volmit.wormholes.util;

import org.bukkit.ChatColor;

public abstract class IOP implements FOP
{
	private DB d;
	protected FileHack h;
	
	public IOP(FileHack h)
	{
		this.h = h;
		d = new DB("IOP<" + getClass().getSimpleName() + ">");
	}
	
	public void queue(FOP f)
	{
		h.queue(f);
	}
	
	@Override
	public void log(String op, CharSequence... s)
	{
		String m = op + ": ";
		
		for(CharSequence i : s)
		{
			m = m + ChatColor.RED + i + ChatColor.YELLOW + " -> ";
		}
		
		d.w(m);
	}
}
