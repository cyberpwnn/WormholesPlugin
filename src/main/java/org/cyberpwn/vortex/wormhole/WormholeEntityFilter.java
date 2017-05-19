package org.cyberpwn.vortex.wormhole;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import wraith.GList;

public class WormholeEntityFilter implements WormholeFilter
{
	private FilterPolicy policy;
	private FilterMode mode;
	private GList<EntityType> entities;
	
	public WormholeEntityFilter(FilterPolicy policy, FilterMode mode, GList<EntityType> entities)
	{
		this.policy = policy;
		this.mode = mode;
		this.entities = entities;
	}
	
	public WormholeEntityFilter(FilterPolicy policy, FilterMode mode, EntityType... entities)
	{
		this(policy, mode, new GList<EntityType>(entities));
	}
	
	@Override
	public boolean onFilter(Wormhole wormhole, Entity e)
	{
		switch(policy)
		{
			case BOTH:
				switch(mode)
				{
					case BLACKLIST:
						if(entities.contains(e.getType()))
						{
							return true;
						}
					case WHITELIST:
						if(!entities.contains(e.getType()))
						{
							return true;
						}
					default:
						break;
				}
			case LOCAL:
				if(wormhole instanceof LocalWormhole)
				{
					switch(mode)
					{
						case BLACKLIST:
							if(entities.contains(e.getType()))
							{
								return true;
							}
						case WHITELIST:
							if(!entities.contains(e.getType()))
							{
								return true;
							}
						default:
							break;
					}
				}
			case MUTEX:
				if(wormhole instanceof MutexWormhole)
				{
					switch(mode)
					{
						case BLACKLIST:
							if(entities.contains(e.getType()))
							{
								return true;
							}
						case WHITELIST:
							if(!entities.contains(e.getType()))
							{
								return true;
							}
						default:
							break;
					}
				}
			default:
				break;
		}
		
		return false;
	}
	
	@Override
	public FilterPolicy getFilterPolicy()
	{
		return policy;
	}
	
	@Override
	public FilterMode getFilterMode()
	{
		return mode;
	}
	
	public FilterPolicy getPolicy()
	{
		return policy;
	}
	
	public FilterMode getMode()
	{
		return mode;
	}
	
	public GList<EntityType> getEntities()
	{
		return entities;
	}
}
