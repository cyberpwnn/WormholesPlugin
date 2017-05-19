package org.cyberpwn.vortex.projection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.portal.Portal;
import wraith.Cuboid;
import wraith.GList;
import wraith.VectorMath;

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
	
	public void rebuild()
	{
		if(Settings.PROJECTION_ACCURACY < 1)
		{
			Settings.PROJECTION_ACCURACY = 1;
		}
		
		if(Settings.PROJECTION_ACCURACY > 24)
		{
			Settings.PROJECTION_ACCURACY = 24;
		}
		
		set = new ProjectionSet();
		Location la = portal.getPosition().getCornerDL();
		Location lb = portal.getPosition().getCornerUR();
		Vector va = VectorMath.direction(getIris(), la);
		Vector vb = VectorMath.direction(getIris(), lb);
		
		for(int i = 0; i < (Settings.PROJECTION_SAMPLE_RADIUS * 2) / Settings.PROJECTION_ACCURACY; i++)
		{
			Location ma = getIris().clone().add(va.clone().multiply(i * Settings.PROJECTION_ACCURACY));
			Location mb = getIris().clone().add(vb.clone().multiply(i * Settings.PROJECTION_ACCURACY));
			set.add(new Cuboid(ma, mb).expand(portal.getIdentity().getBack().f(), Settings.PROJECTION_ACCURACY));
			
			if(set.contains(portal.getPosition().getCenter()))
			{
				set.clear();
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
}
