package org.cyberpwn.vortex.aperture;

import org.bukkit.entity.EntityType;

public class RemoteEntity implements RemoteInstance
{
	private int id;
	private int aid;
	private EntityType remoteType;
	
	public RemoteEntity(int id, EntityType remoteType, int aid)
	{
		this.id = id;
		this.aid = aid;
		this.remoteType = remoteType;
	}
	
	@Override
	public int getRemoteId()
	{
		return id;
	}
	
	@Override
	public EntityType getRemoteType()
	{
		return remoteType;
	}
	
	@Override
	public String getName()
	{
		return remoteType.name();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((remoteType == null) ? 0 : remoteType.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		RemoteEntity other = (RemoteEntity) obj;
		if(id != other.id)
		{
			return false;
		}
		if(remoteType != other.remoteType)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public int getActualId()
	{
		return aid;
	}
}
