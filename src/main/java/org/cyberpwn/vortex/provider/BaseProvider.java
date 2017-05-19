package org.cyberpwn.vortex.provider;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.exception.DuplicatePortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalPositionException;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.portal.PortalIdentity;
import org.cyberpwn.vortex.portal.PortalKey;
import org.cyberpwn.vortex.portal.PortalPosition;
import org.cyberpwn.vortex.projection.BoundingBox;
import org.cyberpwn.vortex.projection.NulledViewport;
import org.cyberpwn.vortex.projection.RasteredSystem;
import org.cyberpwn.vortex.projection.Viewport;
import wraith.C;
import wraith.GList;
import wraith.GMap;
import wraith.NMSX;
import wraith.VectorMath;
import wraith.W;

public abstract class BaseProvider implements PortalProvider
{
	private GList<Player> debuggers;
	private RasteredSystem rasterer;
	private GList<Player> moved;
	
	public BaseProvider()
	{
		rasterer = new RasteredSystem();
		debuggers = new GList<Player>();
		moved = new GList<Player>();
	}
	
	@Override
	public void flush()
	{
		onFlush();
		
		for(Player i : debuggers)
		{
			NMSX.sendActionBar(i, C.LIGHT_PURPLE + getDebugMessage());
		}
	}
	
	public abstract void onFlush();
	
	public abstract String getDebugMessage();
	
	public void toggleDebugging(Player p)
	{
		if(isDebugging(p))
		{
			stopDebugging(p);
		}
		
		else
		{
			startDebugging(p);
		}
	}
	
	public void stopDebugging(Player p)
	{
		if(isDebugging(p))
		{
			debuggers.remove(p);
		}
	}
	
	public void startDebugging(Player p)
	{
		if(!isDebugging(p))
		{
			debuggers.add(p);
		}
	}
	
	public boolean isDebugging(Player p)
	{
		return debuggers.contains(p);
	}
	
	@Override
	public PortalKey buildKey(PortalPosition p) throws InvalidPortalKeyException
	{
		DyeColor u = W.getColor(p.getCenterUp());
		DyeColor d = W.getColor(p.getCenterDown());
		DyeColor l = W.getColor(p.getCenterLeft());
		DyeColor r = W.getColor(p.getCenterRight());
		
		if(u != null && d != null && l != null && r != null)
		{
			return new PortalKey(u, d, l, r);
		}
		
		else
		{
			throw new InvalidPortalKeyException("Invalid portal key");
		}
	}
	
	@Override
	public LocalPortal createPortal(PortalIdentity identity, PortalPosition position) throws InvalidPortalKeyException, InvalidPortalPositionException, DuplicatePortalKeyException
	{
		if(VP.host.isKeyValid(identity.getKey()))
		{
			if(VP.host.isPositionValid(position))
			{
				LocalPortal p = new LocalPortal(identity, position);
				VP.host.addLocalPortal(p);
				return p;
			}
			
			else
			{
				throw new InvalidPortalPositionException("Portal already resides in this area");
			}
		}
		
		else
		{
			throw new DuplicatePortalKeyException("There are already two portal keys of this type that are linked");
		}
	}
	
	@Override
	public void destroyPortal(LocalPortal portal)
	{
		VP.host.removeLocalPortal(portal);
	}
	
	@Override
	public RasteredSystem getRasterer()
	{
		return rasterer;
	}
	
	@Override
	public Viewport getViewport(Player p, Portal portal)
	{
		Viewport v = new Viewport(p, portal);
		v.rebuild();
		return v;
	}
	
	@Override
	public boolean isNear(Player p, Portal portal)
	{
		return portal.getPosition().getBoundingBox().isEnteringOrInside(p);
	}
	
	@Override
	public boolean canSee(Player p, Portal portal)
	{
		return isNear(p, portal) && VectorMath.isLookingNear(p.getLocation(), portal.getPosition().getCenter(), 0.65);
	}
	
	@Override
	public void movePlayer(Player p)
	{
		if(!moved.contains(p))
		{
			moved.add(p);
		}
	}
	
	@Override
	public GMap<Player, Viewport> getViewport(Portal portal)
	{
		GMap<Player, Viewport> views = new GMap<Player, Viewport>();
		BoundingBox box = portal.getPosition().getBoundingBox();
		box.flush();
		
		for(Entity i : box.getInside())
		{
			if(i instanceof Player && moved.contains((Player) i))
			{
				if(portal instanceof LocalPortal)
				{
					if(((LocalPortal) portal).getPosition().getPane().contains(i.getLocation()))
					{
						Player p = (Player) i;
						views.put(p, new NulledViewport(p, portal));
						moved.remove((Player) i);
					}
					
					else
					{
						Player p = (Player) i;
						views.put(p, getViewport(p, portal));
						moved.remove((Player) i);
					}
				}
			}
		}
		
		for(Entity i : box.getExiting())
		{
			if(i instanceof Player)
			{
				Player p = (Player) i;
				views.put(p, new NulledViewport(p, portal));
			}
		}
		
		return views;
	}
}
