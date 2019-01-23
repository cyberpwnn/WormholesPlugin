package com.volmit.wormholes.projection;

import org.bukkit.util.Vector;

public class ArrivalVector
{
	private Vector velocity;
	private Vector direction;
	private Vector offset;
	
	public ArrivalVector(Vector velocity, Vector direction, Vector offset)
	{
		this.velocity = velocity;
		this.direction = direction;
		this.offset = offset;
	}
	
	public Vector getVelocity()
	{
		return velocity;
	}
	
	public void setVelocity(Vector velocity)
	{
		this.velocity = velocity;
	}
	
	public Vector getDirection()
	{
		return direction;
	}
	
	public void setDirection(Vector direction)
	{
		this.direction = direction;
	}
	
	public Vector getOffset()
	{
		return offset;
	}
	
	public void setOffset(Vector offset)
	{
		this.offset = offset;
	}
	
	@Override
	public String toString()
	{
		String o = "";
		o += velocity.getX() + ",";
		o += velocity.getY() + ",";
		o += velocity.getZ() + ",";
		o += direction.getX() + ",";
		o += direction.getY() + ",";
		o += direction.getZ() + ",";
		o += offset.getX() + ",";
		o += offset.getY() + ",";
		o += offset.getZ();
		
		return o;
	}
	
	public void fromString(String s)
	{
		String[] k = s.split(",");
		Double d1 = Double.valueOf(k[0]);
		Double d2 = Double.valueOf(k[1]);
		Double d3 = Double.valueOf(k[2]);
		Double d4 = Double.valueOf(k[3]);
		Double d5 = Double.valueOf(k[4]);
		Double d6 = Double.valueOf(k[5]);
		Double d7 = Double.valueOf(k[6]);
		Double d8 = Double.valueOf(k[7]);
		Double d9 = Double.valueOf(k[8]);
		velocity = new Vector(d1, d2, d3);
		direction = new Vector(d4, d5, d6);
		offset = new Vector(d7, d8, d9);
	}
}
