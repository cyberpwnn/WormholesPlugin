package org.cyberpwn.vortex.portal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.aperture.AperturePlane;
import org.cyberpwn.vortex.event.WormholeLinkEvent;
import org.cyberpwn.vortex.event.WormholeUnlinkEvent;
import org.cyberpwn.vortex.exception.InvalidPortalKeyException;
import org.cyberpwn.vortex.projection.ProjectionPlane;
import org.cyberpwn.vortex.service.MutexService;
import org.cyberpwn.vortex.wormhole.Wormhole;
import wraith.DataCluster;
import wraith.GList;
import wraith.RayTrace;
import wraith.Wraith;

public class LocalPortal implements Portal
{
	private PortalIdentity identity;
	private PortalPosition position;
	private ProjectionPlane plane;
	private String server;
	private Boolean hasBeenValid;
	private Boolean hasHadWormhole;
	private AperturePlane apature;
	private Boolean saved;
	
	public LocalPortal(PortalIdentity identity, PortalPosition position) throws InvalidPortalKeyException
	{
		saved = false;
		hasBeenValid = true;
		hasHadWormhole = false;
		this.identity = identity;
		this.position = position;
		plane = new ProjectionPlane();
		server = "";
		apature = new AperturePlane();
	}
	
	@Override
	public void update()
	{
		if(!hasValidKey())
		{
			VP.host.removeLocalPortal(this);
			return;
		}
		
		if(!saved)
		{
			if(hasValidKey())
			{
				VP.provider.save(this);
				saved = true;
			}
		}
		
		if(hasWormhole())
		{
			if(!hasHadWormhole)
			{
				hasHadWormhole = true;
				Wraith.callEvent(new WormholeLinkEvent(this, getWormhole().getDestination()));
			}
			
			GList<Entity> entities = getPosition().getOPane().getEntities();
			Wormhole w = getWormhole();
			
			for(Entity i : entities)
			{
				if(!getService().isThrottled(i))
				{
					if((i instanceof Player && getPosition().getPane().contains(i.getLocation())) || getPosition().intersects(i.getLocation(), i.getVelocity()))
					{
						getService().addThrottle(i);
						w.push(i);
					}
				}
			}
		}
		
		else if(hasHadWormhole)
		{
			hasHadWormhole = false;
			VP.projector.deproject(this);
			Wraith.callEvent(new WormholeUnlinkEvent(this));
		}
		
		if(!plane.hasContent())
		{
			plane.sample(getPosition().getCenter().clone(), Settings.PROJECTION_SAMPLE_RADIUS);
		}
	}
	
	public boolean isPlayerLookingAt(Player i)
	{
		if(!getPosition().getCenter().getWorld().equals(i.getWorld()))
		{
			return false;
		}
		
		double dis = i.getLocation().clone().add(0, 1.7, 0).distance(getPosition().getCenter()) + 7;
		Vector dir = i.getLocation().getDirection();
		
		boolean[] b = {false};
		
		new RayTrace(i.getLocation().clone().add(0, 1.7, 0), dir, dis, 0.75)
		{
			@Override
			public void onTrace(Location location)
			{
				if(getPosition().getPane().contains(location))
				{
					stop();
					b[0] = true;
				}
			}
		}.trace();
		
		return b[0];
	}
	
	public GList<Player> getPlayersLookingAt()
	{
		GList<Player> players = new GList<Player>();
		
		for(Player i : getPosition().getArea().getPlayers())
		{
			double dis = i.getLocation().clone().add(0, 1.7, 0).distance(getPosition().getCenter()) + 7;
			Vector dir = i.getLocation().getDirection();
			
			new RayTrace(i.getLocation().clone().add(0, 1.7, 0), dir, dis, 0.75)
			{
				@Override
				public void onTrace(Location location)
				{
					if(getPosition().getPane().contains(location))
					{
						stop();
						players.add(i);
					}
				}
			}.trace();
		}
		
		return players;
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
	
	@Override
	public AperturePlane getApature()
	{
		return apature;
	}
	
	public void destroy()
	{
		getPosition().getCenter().getWorld().createExplosion(getPosition().getCenter(), 0f);
		getPosition().getCenterDown().getBlock().setType(Material.AIR);
		getPosition().getCenterUp().getBlock().setType(Material.AIR);
		getPosition().getCenterLeft().getBlock().setType(Material.AIR);
		getPosition().getCenterRight().getBlock().setType(Material.AIR);
	}
}
