package org.cyberpwn.vortex.portal;

import org.bukkit.entity.Entity;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.aperture.AperturePlane;
import org.cyberpwn.vortex.exception.InvalidPortalKeyException;
import org.cyberpwn.vortex.projection.ProjectionPlane;
import org.cyberpwn.vortex.service.MutexService;
import org.cyberpwn.vortex.wormhole.Wormhole;
import wraith.DataCluster;
import wraith.GList;

public class LocalPortal implements Portal
{
	private PortalIdentity identity;
	private PortalPosition position;
	private ProjectionPlane plane;
	private String server;
	private Boolean hasBeenValid;
	private AperturePlane apature;
	
	public LocalPortal(PortalIdentity identity, PortalPosition position) throws InvalidPortalKeyException
	{
		hasBeenValid = true;
		this.identity = identity;
		this.position = position;
		plane = new ProjectionPlane();
		server = "";
		apature = new AperturePlane();
	}
	
	@Override
	public void update()
	{
		if(hasWormhole())
		{
			GList<Entity> entities = getPosition().getPane().getEntities();
			Wormhole w = getWormhole();
			
			for(Entity i : entities)
			{
				if(!getService().isThrottled(i))
				{
					getService().addThrottle(i);
					w.push(i);
				}
			}
		}
		
		if(!plane.hasContent())
		{
			plane.sample(getPosition().getCenter().clone(), 25);
		}
	}
	
	@Override
	public PortalIdentity getIdentity()
	{
		return identity;
	}
	
	@Override
	public PortalPosition getPosition()
	{
		return position;
	}
	
	@Override
	public PortalKey getKey()
	{
		return identity.getKey();
	}
	
	@Override
	public boolean hasWormhole()
	{
		if(!hasValidKey())
		{
			return false;
		}
		
		return getService().hasWormhole(this);
	}
	
	@Override
	public boolean isWormholeMutex()
	{
		if(!hasValidKey())
		{
			return false;
		}
		
		return getService().isMutexWormhole(this);
	}
	
	@Override
	public Wormhole getWormhole()
	{
		if(!hasValidKey())
		{
			return null;
		}
		
		return getService().getWormhole(this);
	}
	
	@Override
	public MutexService getService()
	{
		return VP.host;
	}
	
	@Override
	public DataCluster toData()
	{
		DataCluster cc = new DataCluster();
		
		cc.set("ku", getKey().getU().ordinal());
		cc.set("kd", getKey().getD().ordinal());
		cc.set("kl", getKey().getL().ordinal());
		cc.set("kr", getKey().getR().ordinal());
		cc.set("if", getIdentity().getFront().ordinal());
		
		return cc;
	}
	
	@Override
	public void fromData(DataCluster cc)
	{
		
	}
	
	@Override
	public String getServer()
	{
		if(server.equals("") && VP.bus.isOnline())
		{
			server = VP.bus.getServerName();
		}
		
		return server;
	}
	
	@Override
	public ProjectionPlane getProjectionPlane()
	{
		return plane;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
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
		LocalPortal other = (LocalPortal) obj;
		if(identity == null)
		{
			if(other.identity != null)
			{
				return false;
			}
		}
		else if(!identity.equals(other.identity))
		{
			return false;
		}
		if(position == null)
		{
			if(other.position != null)
			{
				return false;
			}
		}
		else if(!position.equals(other.position))
		{
			return false;
		}
		if(server == null)
		{
			if(other.server != null)
			{
				return false;
			}
		}
		else if(!server.equals(other.server))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean hasValidKey()
	{
		try
		{
			PortalKey k = VP.provider.buildKey(getPosition());
			
			if(VP.host.isKeyValidAlready(k))
			{
				identity.setKey(k);
				hasBeenValid = true;
				return true;
			}
			
			else
			{
				if(hasBeenValid)
				{
					getService().dequeue(this);
					hasBeenValid = false;
				}
				
				return false;
			}
		}
		
		catch(InvalidPortalKeyException e)
		{
			if(hasBeenValid)
			{
				getService().dequeue(this);
				hasBeenValid = false;
			}
			
			return false;
		}
	}
	
	public ProjectionPlane getPlane()
	{
		return plane;
	}
	
	public Boolean getHasBeenValid()
	{
		return hasBeenValid;
	}
	
	public AperturePlane getApature()
	{
		return apature;
	}
}
