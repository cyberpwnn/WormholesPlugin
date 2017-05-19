package org.cyberpwn.vortex.portal;

import org.bukkit.DyeColor;
import wraith.C;
import wraith.SYM;

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
		
		if(d != other.d)
		{
			return false;
		}
		
		if(l != other.l)
		{
			return false;
		}
		
		if(r != other.r)
		{
			return false;
		}
		
		if(u != other.u)
		{
			return false;
		}
		
		return true;
	}
}
