package com.volmit.wormholes.portal;

import com.volmit.wormholes.util.Axis;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.VectorMath;

public class PortalIdentity
{
	private Axis axis;
	private Direction front;
	private Direction back;
	private Direction up;
	private Direction down;
	private Direction left;
	private Direction right;
	private PortalKey key;
	
	public PortalIdentity(Direction f, PortalKey k)
	{
		back = f;
		front = f.reverse();
		left = f.isVertical() ? Direction.getDirection(VectorMath.rotate90CCZ(front.toVector())) : Direction.getDirection(VectorMath.rotate90CY(front.toVector()));
		right = f.isVertical() ? Direction.getDirection(VectorMath.rotate90CZ(front.toVector())) : Direction.getDirection(VectorMath.rotate90CCY(front.toVector()));
		up = f.isVertical() ? Direction.news().qdel(left).qdel(right).get(0) : Direction.U;
		down = f.isVertical() ? Direction.news().qdel(left).qdel(right).get(1) : Direction.D;
		axis = getAx();
		key = k;
	}
	
	@Override
	public String toString()
	{
		return back + axis.toString() + front + ":" + up + "" + down + ":" + left + "" + right;
	}
	
	private Axis getAx()
	{
		for(Axis i : Axis.values())
		{
			if(i.positive().equals(front.toVector()) || i.positive().equals(back.toVector()))
			{
				return i;
			}
		}
		
		return null;
	}
	
	public Direction getFront()
	{
		return front;
	}
	
	public Direction getBack()
	{
		return back;
	}
	
	public Direction getUp()
	{
		return up;
	}
	
	public Direction getDown()
	{
		return down;
	}
	
	public Direction getLeft()
	{
		return left;
	}
	
	public Direction getRight()
	{
		return right;
	}
	
	public Axis getAxis()
	{
		return axis;
	}
	
	public PortalKey getKey()
	{
		return key;
	}
	
	public void setKey(PortalKey k)
	{
		key = k;
	}
}
