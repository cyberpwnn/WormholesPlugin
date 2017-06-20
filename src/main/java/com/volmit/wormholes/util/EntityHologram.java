package com.volmit.wormholes.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Represents an entity hologram
 * 
 * @author cyberpwn
 */
public class EntityHologram implements Hologram
{
	public static GList<Entity> lock = new GList<Entity>();
	private Entity base;
	private Player exc;
	
	/**
	 * Create a new entity hologram holder out of an entity
	 * 
	 * @param base
	 *            the entity
	 */
	public EntityHologram(Entity base)
	{
		exc = null;
		this.base = base;
		lock.add(base);
	}
	
	@Override
	public Entity getHandle()
	{
		return base;
	}
	
	@Override
	public String getDisplay()
	{
		return getHandle().getCustomName();
	}
	
	@Override
	public void setDisplay(String display)
	{
		if(display == null)
		{
			getHandle().setCustomNameVisible(false);
			return;
		}
		
		getHandle().setCustomName(display);
		getHandle().setCustomNameVisible(true);
	}
	
	@Override
	public void setLocation(Location location)
	{
		getHandle().teleport(location);
	}
	
	@Override
	public void setTextLocation(Location location)
	{
		setLocation(location.clone().add(0, -0.87, 0));
	}
	
	@Override
	public void destroy()
	{
		getHandle().remove();
		lock.remove(base);
	}
	
	@Override
	public Location getLocation()
	{
		return getHandle().getLocation();
	}
	
	@Override
	public void setExclusive(Player p)
	{
		exc = p;
	}
	
	@Override
	public Player getExclusive()
	{
		return exc;
	}
}
