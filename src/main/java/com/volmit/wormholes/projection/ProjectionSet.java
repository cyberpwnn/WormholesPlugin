package com.volmit.wormholes.projection;

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
	
	public ProjectionSet(GList<Cuboid> c)
	{
		this();
		
		add(c);
	}
	
	public Cuboid pop()
	{
		Cuboid c = cuboids.pop();
		
		return c;
	}
	
	public void remove(int amt)
	{
		for(int i = 0; i < amt; i++)
		{
			cuboids.remove(0);
		}
	}
	
	public void keep(int amt)
	{
		for(int i = 0; i < size() - amt; i++)
		{
			cuboids.remove(size() - 1);
		}
	}
	
	public Cuboid get(int ind)
	{
		return cuboids.get(ind);
	}
	
	public int size()
	{
		return cuboids.size();
	}
	
	public void add(GList<Cuboid> c)
	{
		cuboids.add(c);
	}
	
	public void add(Cuboid c)
	{
		cuboids.add(c);
	}
	
	public GList<Cuboid> get()
	{
		return cuboids;
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
	
	public ProjectionSet copy()
	{
		return new ProjectionSet(cuboids.copy());
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
