package com.volmit.wormholes.projection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.VectorMath;

public class Viewport
{
	private Player p;
	private Portal portal;
	private ProjectionSet set;
	
	public Viewport(Player p, Portal portal)
	{
		this.p = p;
		this.portal = portal;
		set = new ProjectionSet();
	}
	
	public void wipe()
	{
		for(Block i : set.getBlocks())
		{
			Wormholes.provider.getRasterer().dequeue(p, i.getLocation());
		}
	}
	
	public void rebuild()
	{
		set = new ProjectionSet();
		Location la = portal.getPosition().getCornerDL();
		Location lb = portal.getPosition().getCornerUR();
		Vector va = VectorMath.direction(getIris(), la);
		Vector vb = VectorMath.direction(getIris(), lb);
		
		for(int i = 0; i < Settings.PROJECTION_SAMPLE_RADIUS + 6; i++)
		{
			Location ma = getIris().clone().add(va.clone().multiply(i));
			Location mb = getIris().clone().add(vb.clone().multiply(i));
			set.add(new Cuboid(ma, mb));
			
			if(set.contains(portal.getPosition().getCenter()))
			{
				set.clear();
			}
		}
		
		for(Cuboid i : set.get().copy())
		{
			if(i.getCenter().distance(p.getLocation()) < portal.getPosition().getCenter().distance(p.getLocation()))
			{
				set.clear();
				return;
			}
		}
	}
	
	public GList<Entity> getEntities()
	{
		return set.getEntities();
	}
	
	public boolean contains(Location l)
	{
		if(portal.getPosition().getPane().contains(l))
		{
			return false;
		}
		
		return set.contains(l);
	}
	
	public Location getIris()
	{
		return p.getLocation().clone().add(0, 1.7, 0);
	}
	
	public Player getP()
	{
		return p;
	}
	
	public Portal getPortal()
	{
		return portal;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((portal == null) ? 0 : portal.hashCode());
		result = prime * result + ((set == null) ? 0 : set.hashCode());
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
		Viewport other = (Viewport) obj;
		if(p == null)
		{
			if(other.p != null)
			{
				return false;
			}
		}
		else if(!p.equals(other.p))
		{
			return false;
		}
		if(portal == null)
		{
			if(other.portal != null)
			{
				return false;
			}
		}
		else if(!portal.equals(other.portal))
		{
			return false;
		}
		if(set == null)
		{
			if(other.set != null)
			{
				return false;
			}
		}
		else if(!set.equals(other.set))
		{
			return false;
		}
		return true;
	}
	
	public ProjectionSet getProjectionSet()
	{
		return set;
	}
}
