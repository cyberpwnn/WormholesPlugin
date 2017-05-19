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
}
