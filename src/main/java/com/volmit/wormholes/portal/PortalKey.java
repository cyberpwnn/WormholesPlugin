package com.volmit.wormholes.portal;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import com.volmit.wormholes.util.Axis;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.SYM;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.W;

public class PortalKey
{
	private DyeColor u;
	private DyeColor d;
	private DyeColor l;
	private DyeColor r;
	
	public PortalKey(byte[] b)
	{
		u = DyeColor.values()[b[0]];
		d = DyeColor.values()[b[1]];
		l = DyeColor.values()[b[2]];
		r = DyeColor.values()[b[3]];
	}
	
	public PortalKey(DyeColor u, DyeColor d, DyeColor l, DyeColor r)
	{
		this.u = u;
		this.d = d;
		this.l = l;
		this.r = r;
	}
	
	public String getSName()
	{
		return (int) toData()[0] + "," + (int) toData()[1] + "," + (int) toData()[2] + "," + (int) toData()[3];
	}
	
	public static PortalKey fromSName(String s)
	{
		return new PortalKey(new byte[] {Integer.valueOf(s.split(",")[0]).byteValue(), Integer.valueOf(s.split(",")[1]).byteValue(), Integer.valueOf(s.split(",")[2]).byteValue(), Integer.valueOf(s.split(",")[3]).byteValue()});
	}
	
	public DyeColor getU()
	{
		return u;
	}
	
	public void setU(DyeColor u)
	{
		this.u = u;
	}
	
	public DyeColor getD()
	{
		return d;
	}
	
	public void setD(DyeColor d)
	{
		this.d = d;
	}
	
	public DyeColor getL()
	{
		return l;
	}
	
	public void setL(DyeColor l)
	{
		this.l = l;
	}
	
	public DyeColor getR()
	{
		return r;
	}
	
	public void setR(DyeColor r)
	{
		this.r = r;
	}
	
	public byte[] toData()
	{
		return new byte[] {(byte) u.ordinal(), (byte) d.ordinal(), (byte) l.ordinal(), (byte) r.ordinal()};
	}
	
	@Override
	public String toString()
	{
		String s = SYM.SHAPE_SQUARE + "";
		
		return C.dyeToChat(u) + s + C.dyeToChat(d) + s + C.dyeToChat(l) + s + C.dyeToChat(r) + s;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + ((r == null) ? 0 : r.hashCode());
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		
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
		
		PortalKey other = (PortalKey) obj;
		
		if(!d.equals(other.d))
		{
			return false;
		}
		
		if(!l.equals(other.l))
		{
			return false;
		}
		
		if(!r.equals(other.r))
		{
			return false;
		}
		
		if(!u.equals(other.u))
		{
			return false;
		}
		
		return true;
	}
	
	public void applyToCuboid(Cuboid c, Direction d)
	{
		Axis a = d.getAxis();
		Direction f = d;
		Direction front = f.reverse();
		Direction left = f.isVertical() ? Direction.getDirection(VectorMath.rotate90CCZ(front.toVector())) : Direction.getDirection(VectorMath.rotate90CY(front.toVector()));
		Direction right = f.isVertical() ? Direction.getDirection(VectorMath.rotate90CZ(front.toVector())) : Direction.getDirection(VectorMath.rotate90CCY(front.toVector()));
		Direction up = f.isVertical() ? Direction.news().qdel(left).qdel(right).get(0) : Direction.U;
		Direction down = f.isVertical() ? Direction.news().qdel(left).qdel(right).get(1) : Direction.D;
		
		for(Direction i : Direction.udnews())
		{
			if(i.getAxis().equals(a))
			{
				continue;
			}
			
			Cuboid cx = c.getFace(i.f());
			DyeColor dd = null;
			
			if(i.equals(down))
			{
				dd = getD();
			}
			
			else if(i.equals(up))
			{
				dd = getU();
			}
			
			else if(i.equals(left))
			{
				dd = getL();
			}
			
			else
			{
				dd = getR();
			}
			
			if(!W.isColorable(cx.getCenter().getBlock()))
			{
				cx.getCenter().getBlock().setType(Material.WOOL);
			}
			
			W.setColor(cx.getCenter().getBlock(), dd);
		}
	}
}
