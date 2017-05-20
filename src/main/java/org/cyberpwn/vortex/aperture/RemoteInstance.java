package org.cyberpwn.vortex.aperture;

import java.util.UUID;
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
				return new RemotePlayer(40978 - i.getEntityId(), ((Player) i).getName(), UUID.nameUUIDFromBytes(i.getUniqueId().toString().getBytes()));
			}
			
			return new RemoteEntity((int) (4097800 - i.getEntityId()), i.getType());
		}
		
		return null;
	}
}