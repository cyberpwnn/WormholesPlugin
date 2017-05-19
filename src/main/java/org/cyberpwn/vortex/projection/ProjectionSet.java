package org.cyberpwn.vortex.projection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import wraith.Cuboid;
import wraith.GList;

public class ProjectionSet
{
	public GList<Cuboid> cuboids;
	
	public ProjectionSet()
	{
		cuboids = new GList<Cuboid>();
	}
	
	public void add(Cuboid c)
	{
		cuboids.add(c);
	}
	
	public GList<Cuboid> get()
	{
		return cuboids.copy();
	}
	
	public GList<Block> getBlocks()
	{
		GList<Block> blocks = new GList<Block>();
		
		for(Cuboid i : get())
		{
			blocks.add(new GList<Block>(i.iterator()));
		}
		
		return blocks;
	}
	
	public GList<Entity> getEntities()
	{
		GList<Entity> entities = new GList<Entity>();
		
		for(Cuboid i : get())
		{
			entities.add(i.getEntities());
		}
		
		entities.removeDuplicates();
		
		return entities;
	}
	
	public boolean contains(Location l)
	{
		if(l == null)
		{
			return false;
		}
		
		for(Cuboid i : get())
		{
			if(i.contains(l))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cuboids == null) ? 0 : cuboids.hashCode());
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
		ProjectionSet other = (ProjectionSet) obj;
		if(cuboids == null)
		{
			if(other.cuboids != null)
			{
				return false;
			}
		}
		else if(!cuboids.equals(other.cuboids))
		{
			return false;
		}
		return true;
	}
	
	public void clear()
	{
		cuboids.clear();
	}
}
