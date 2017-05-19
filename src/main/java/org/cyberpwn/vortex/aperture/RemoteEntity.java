package org.cyberpwn.vortex.aperture;

import org.bukkit.entity.EntityType;

public class RemoteEntity implements RemoteInstance
{
	private int id;
	private EntityType remoteType;
	
	public RemoteEntity(int id, EntityType remoteType)
	{
		this.id = id;
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
}
