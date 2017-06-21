package com.volmit.wormholes.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Frustrum
{
	private int ox;
	private int oy;
	private int oz;
	private int maxX;
	private int maxY;
	private int maxZ;
	private int minX;
	private int minY;
	private int minZ;
	private World world;
	private Vector vMin;
	private Vector vMax;
	private Vector vDirect;
	private Location origin;
	private Cuboid frame;
	private Direction direction;
	private Axis axis;
	
	public Frustrum(World world, int ox, int oy, int oz, int maxX, int maxY, int maxZ, int minX, int minY, int minZ)
	{
		this.world = world;
		this.ox = ox;
		this.oy = oy;
		this.oz = oz;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		origin = new Location(world, ox, oy, oz);
		frame = new Cuboid(world, maxX, maxY, maxZ, minX, minY, minZ);
		vMax = VectorMath.direction(origin, frame.getLowerNE());
		vMin = VectorMath.direction(origin, frame.getUpperSW());
		vDirect = VectorMath.direction(origin, frame.getCenter());
		direction = Direction.getDirection(vDirect.clone());
		axis = direction.getAxis();
	}
	
	public Frustrum(Location origin, Location max, Location min)
	{
		this(origin.getWorld(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ(), min.getBlockX(), min.getBlockY(), min.getBlockZ());
	}
	
	public Frustrum(Location origin, Cuboid face)
	{
		this(origin, face.getLowerNE(), face.getUpperSW());
	}
	
	public GList<Player> getPlayers(int range)
	{
		return new GList<Player>(new GListAdapter<Entity, Player>()
		{
			@Override
			public Player onAdapt(Entity from)
			{
				if(from instanceof Player)
				{
					return (Player) from;
				}
				
				return null;
			}
		}.adapt(getEntities(range)));
	}
	
	public GList<Entity> getEntities(int range)
	{
		if(range < 1)
		{
			range = 1;
		}
		
		GList<Entity> e = new GList<Entity>();
		
		for(Chunk i : W.chunkRadius(origin.getChunk(), range / 16))
		{
			for(Entity j : i.getEntities())
			{
				if(w(j.getLocation()))
				{
					e.add(j);
				}
			}
		}
		
		return e;
	}
	
	public GList<Block> getBlocks(int distance)
	{
		GList<Block> blocks = new GList<Block>();
		
		while(distance > 0)
		{
			int bias = distance;
			
			switch(direction)
			{
				case D:
					bias = -bias;
				case N:
					bias = -bias;
				case W:
					bias = -bias;
				default:
					break;
			}
			
			Vector sMax = VectorMath.scaleStatic(axis, vMax, bias);
			Vector sMin = VectorMath.scaleStatic(axis, vMin, bias);
			blocks.add(new GList<Block>(new Cuboid(getOrigin().clone().add(sMax), getOrigin().clone().add(sMin)).iterator()));
			distance--;
		}
		
		return blocks;
	}
	
	public boolean w(Location l)
	{
		int bias = 0;
		int seperation = 0;
		
		switch(axis)
		{
			case X:
				bias = l.getBlockX() - origin.getBlockX();
				seperation = frame.getCenter().getBlockX() - origin.getBlockX();
			case Y:
				bias = l.getBlockY() - origin.getBlockY();
				seperation = frame.getCenter().getBlockY() - origin.getBlockY();
			case Z:
				bias = l.getBlockZ() - origin.getBlockZ();
				seperation = frame.getCenter().getBlockZ() - origin.getBlockZ();
		}
		
		if(Math.abs(bias) < Math.abs(seperation))
		{
			return false;
		}
		
		Vector sMax = VectorMath.scaleStatic(axis, vMax, bias);
		Vector sMin = VectorMath.scaleStatic(axis, vMin, bias);
		Vector sVin = VectorMath.direction(getOrigin(), l);
		
		switch(axis)
		{
			case X:
				return w(sMax.getBlockY(), sMin.getBlockY(), sVin.getBlockY()) && w(sMax.getBlockZ(), sMin.getBlockZ(), sVin.getBlockZ());
			case Y:
				return w(sMax.getBlockX(), sMin.getBlockX(), sVin.getBlockX()) && w(sMax.getBlockZ(), sMin.getBlockZ(), sVin.getBlockZ());
			case Z:
				return w(sMax.getBlockY(), sMin.getBlockY(), sVin.getBlockY()) && w(sMax.getBlockX(), sMin.getBlockX(), sVin.getBlockX());
		}
		
		return false;
	}
	
	public boolean w(int a, int b, int i)
	{
		return i <= Math.max(a, b) && i >= Math.min(a, b);
	}
	
	public int getOx()
	{
		return ox;
	}
	
	public int getOy()
	{
		return oy;
	}
	
	public int getOz()
	{
		return oz;
	}
	
	public int getMaxX()
	{
		return maxX;
	}
	
	public int getMaxY()
	{
		return maxY;
	}
	
	public int getMaxZ()
	{
		return maxZ;
	}
	
	public int getMinX()
	{
		return minX;
	}
	
	public int getMinY()
	{
		return minY;
	}
	
	public int getMinZ()
	{
		return minZ;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public Vector getvMin()
	{
		return vMin;
	}
	
	public Vector getvMax()
	{
		return vMax;
	}
	
	public Vector getvDirect()
	{
		return vDirect;
	}
	
	public Location getOrigin()
	{
		return origin;
	}
	
	public Cuboid getFrame()
	{
		return frame;
	}
	
	public Direction getDirection()
	{
		return direction;
	}
	
	public Axis getAxis()
	{
		return axis;
	}
}
