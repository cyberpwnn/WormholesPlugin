package org.cyberpwn.vortex.aperture;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public interface RemoteInstance
{
	public int getRemoteId();
	
	public EntityType getRemoteType();
	
	public String getName();
	
	public static RemoteInstance create(Entity i)
	{
		if(i != null)
		{
			if(i.getType().equals(EntityType.PLAYER))
			{
				return new RemotePlayer(i.getEntityId(), ((Player) i).getName(), i.getUniqueId());
			}
			
			return new RemoteEntity((int) (-i.getEntityId()), i.getType());
		}
		
		return null;
	}
}
