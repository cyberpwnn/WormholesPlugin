package com.volmit.wormholes.aperture;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public interface RemoteInstance
{
	public int getRemoteId();
	
	public EntityType getRemoteType();
	
	public String getName();
	
	public int getActualId();
	
	public static RemoteInstance create(Entity i)
	{
		if(i != null)
		{
			if(i.getType().equals(EntityType.PLAYER))
			{
				return new RemotePlayer(2097800 + i.getEntityId(), ((Player) i).getName(), ((Player) i).getUniqueId(), i.getEntityId());
			}
			
			return new RemoteEntity((int) (4097800 + i.getEntityId()), i.getType(), i.getEntityId());
		}
		
		return null;
	}
}
