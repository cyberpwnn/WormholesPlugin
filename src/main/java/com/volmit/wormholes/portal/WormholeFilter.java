package com.volmit.wormholes.portal;

import org.bukkit.entity.Entity;

public interface WormholeFilter
{
	public boolean onFilter(Wormhole wormhole, Entity e);
	
	public FilterPolicy getFilterPolicy();
	
	public FilterMode getFilterMode();
}
