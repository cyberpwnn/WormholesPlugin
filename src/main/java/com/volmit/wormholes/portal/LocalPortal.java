package com.volmit.wormholes.portal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.aperture.AperturePlane;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.event.WormholeLinkEvent;
import com.volmit.wormholes.event.WormholeUnlinkEvent;
import com.volmit.wormholes.exception.DuplicatePortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalPositionException;
import com.volmit.wormholes.projection.ProjectionMask;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.service.MutexService;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.Intersection;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.RayTrace;
import com.volmit.wormholes.util.TaskLater;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.Wraith;
import com.volmit.wormholes.wormhole.Wormhole;

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
	private PortalSettings settings;
	private ProjectionMask mask;
	
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
		settings = new PortalSettings();
		mask = new ProjectionMask();
	}
	
	@Override
	public void update()
	{
		if(!hasValidIshKey())
		{
			Wormholes.host.removeLocalPortal(this);
			return;
		}
		
		if(!saved)
		{
			if(hasValidKey())
			{
				Wormholes.provider.save(this);
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
			
			if(M.r(0.9))
			{
				Wormholes.fx.rise(this);
			}
			
			if(M.r(0.07))
			{
				Wormholes.fx.ambient(this);
			}
			
			checkFrame();
		}
		
		else if(hasHadWormhole)
		{
			hasHadWormhole = false;
			Wormholes.projector.deproject(this);
			Wraith.callEvent(new WormholeUnlinkEvent(this));
		}
		
		if(!plane.hasContent())
		{
			plane.sample(getPosition().getCenter().clone(), Settings.PROJECTION_SAMPLE_RADIUS, getIdentity().getFront().isVertical());
			specialUpdate();
		}
	}
	
	public void checkFrame(Entity i)
	{
		Wormhole w = getWormhole();
		
		if(!getService().isThrottled(i))
		{
			if(i instanceof Player)
			{
				if(getPosition().getPane().contains(i.getLocation()))
				{
					if(new Permissable(((Player) i)).canUse())
					{
						getService().addThrottle(i);
						w.push(i);
					}
					
					else
					{
						Wormholes.fx.throwBack(i, Wormholes.fx.throwBackVector(i, this), this);
					}
				}
			}
			
			else if(getPosition().getPane().contains(i.getLocation()))
			{
				checkSend(i, w);
			}
		}
	}
	
	public void throwBack(Entity i)
	{
		Wormholes.fx.throwBack(i, Wormholes.fx.throwBackVector(i, this), this);
	}
	
	public void checkSend(Entity i, Wormhole w)
	{
		if(w == null)
		{
			return;
		}
		
		boolean[] wx = {false};
		
		if(getPosition().getPane().contains(i.getLocation()))
		{
			wx[0] = true;
		}
		
		if(!wx[0])
		{
			wx[0] = Intersection.intersects(getPosition(), i.getLocation(), i.getVelocity().clone().multiply(10));
		}
		
		if(!wx[0])
		{
			return;
		}
		
		if(i.getType().equals(EntityType.ARMOR_STAND))
		{
			return;
		}
		
		if(!Settings.ALLOW_ENTITIES)
		{
			throwBack(i);
		}
		
		else if(!settings.isAllowEntities())
		{
			throwBack(i);
		}
		
		else if(Settings.ALLOW_ENTITIY_TYPES.contains(i.getType().toString()))
		{
			send(i, w);
		}
		
		else
		{
			throwBack(i);
		}
	}
	
	public void send(Entity i, Wormhole w)
	{
		getService().addThrottle(i);
		w.push(i);
	}
	
	public void checkFrame()
	{
		GList<Entity> entities = getPosition().getOPane().getEntities();
		
		for(Entity i : entities)
		{
			checkFrame(i);
		}
	}
	
	public void specialUpdate()
	{
		for(Player i : getPosition().getArea().getPlayers())
		{
			mask.sched(i);
			
			new TaskLater(20)
			{
				@Override
				public void run()
				{
					mask.sched(i);
				}
			};
		}
	}
	
	public void reversePolarity()
	{
		try
		{
			PortalPosition p = getPosition();
			PortalPosition n = new PortalPosition(new PortalIdentity(p.getIdentity().getFront(), getKey()), p.getPane());
			PortalKey pk;
			pk = Wormholes.provider.buildKey(n);
			n.getIdentity().setKey(pk);
			
			new TaskLater()
			{
				@Override
				public void run()
				{
					Wormholes.host.removeLocalPortalReverse(LocalPortal.this);
					
					new TaskLater()
					{
						@Override
						public void run()
						{
							try
							{
								Wormholes.provider.createPortal(n.getIdentity(), n);
								
								new TaskLater(2)
								{
									@Override
									public void run()
									{
										specialUpdate();
									}
								};
							}
							
							catch(InvalidPortalKeyException | InvalidPortalPositionException | DuplicatePortalKeyException e)
							{
								e.printStackTrace();
							}
						}
					};
				}
			};
		}
		
		catch(InvalidPortalKeyException e)
		{
			e.printStackTrace();
		}
	}
	
	public Direction getThrowDirection(Location l)
	{
		if(!getIdentity().getFront().isVertical())
		{
			l.setY(getPosition().getCenter().getY());
			Vector v = VectorMath.direction(getPosition().getCenter(), l);
			return Direction.getDirection(v);
		}
		
		Vector v = VectorMath.direction(getPosition().getCenter(), l);
		return Direction.getDirection(v);
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
		return Wormholes.host;
	}
	
	@Override
	public DataCluster toData()
	{
		DataCluster cc = new DataCluster();
		
		cc.set("ku", getKey().getU().ordinal());
		cc.set("kd", getKey().getD().ordinal());
		cc.set("kl", getKey().getL().ordinal());
		cc.set("kr", getKey().getR().ordinal());
		cc.set("kx", getKey().getSName() + "vxx");
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
		if(server.equals("") && Wormholes.bus.isOnline())
		{
			server = Wormholes.bus.getServerName();
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
	
	public boolean hasValidIshKey()
	{
		try
		{
			Wormholes.provider.buildKey(getPosition());
		}
		
		catch(InvalidPortalKeyException e)
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
			PortalKey k = Wormholes.provider.buildKey(getPosition());
			
			if(Wormholes.host.isKeyValidAlready(k))
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
		Wormholes.fx.destroyed(this);
		getPosition().getCenterDown().getBlock().setType(Material.AIR);
		getPosition().getCenterUp().getBlock().setType(Material.AIR);
		getPosition().getCenterLeft().getBlock().setType(Material.AIR);
		getPosition().getCenterRight().getBlock().setType(Material.AIR);
	}
	
	public Boolean getHasHadWormhole()
	{
		return hasHadWormhole;
	}
	
	public Boolean getSaved()
	{
		return saved;
	}
	
	public PortalSettings getSettings()
	{
		return settings;
	}
	
	public ProjectionMask getMask()
	{
		return mask;
	}
}
