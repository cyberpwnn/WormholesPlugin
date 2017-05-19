package org.cyberpwn.vortex.projection;

import org.bukkit.entity.Entity;
import wraith.Cuboid;
import wraith.GList;

public class BoundingBox
{
	private GList<Entity> inside;
	private GList<Entity> entering;
	private GList<Entity> exiting;
	private Cuboid cuboid;
	
	public BoundingBox(Cuboid cuboid)
	{
		this.cuboid = cuboid;
		inside = new GList<Entity>();
		exiting = new GList<Entity>();
		entering = new GList<Entity>();
	}
	
	public void flush()
	{
		try
		{
			GList<Entity> currentlyInside = cuboid.getEntities();
			
			exiting.clear();
			inside.add(entering);
			inside.removeDuplicates();
			entering.clear();
			
			for(Entity i : currentlyInside)
			{
				if(!inside.contains(i) && !entering.contains(i) && !exiting.contains(i))
				{
					entering.add(i);
				}
			}
			
			for(Entity i : inside.copy())
			{
				if(!currentlyInside.contains(i))
				{
					inside.remove(i);
					exiting.add(i);
				}
			}
		}
		
		catch(Exception e)
		{
			
		}
	}
	
	public boolean isInside(Entity e)
	{
		return inside.contains(e);
	}
	
	public boolean isEntering(Entity e)
	{
		return entering.contains(e);
	}
	
	public boolean isEnteringOrInside(Entity e)
	{
		return isEntering(e) || isInside(e);
	}
	
	public boolean isExiting(Entity e)
	{
		return exiting.contains(e);
	}
	
	public GList<Entity> getInside()
	{
		return inside;
	}
	
	public GList<Entity> getEntering()
	{
		return entering;
	}
	
	public GList<Entity> getExiting()
	{
		return exiting;
	}
	
	public Cuboid getCuboid()
	{
		return cuboid;
	}
}
