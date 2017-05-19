package org.cyberpwn.vortex.aperture;

import java.util.UUID;
import org.bukkit.entity.EntityType;

public class RemotePlayer extends RemoteEntity
{
	private String name;
	private UUID uuid;
	
	public RemotePlayer(int id, String name, UUID uuid)
	{
		super(id, EntityType.PLAYER);
		
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	public UUID getUuid()
	{
		return uuid;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(!super.equals(obj))
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		RemotePlayer other = (RemotePlayer) obj;
		if(name == null)
		{
			if(other.name != null)
			{
				return false;
			}
		}
		else if(!name.equals(other.name))
		{
			return false;
		}
		if(uuid == null)
		{
			if(other.uuid != null)
			{
				return false;
			}
		}
		else if(!uuid.equals(other.uuid))
		{
			return false;
		}
		return true;
	}
}
