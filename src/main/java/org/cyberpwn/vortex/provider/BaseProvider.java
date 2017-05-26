package org.cyberpwn.vortex.provider;

import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.event.WormholeCreateEvent;
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
import wraith.Cuboid;
import wraith.DataCluster;
import wraith.Direction;
import wraith.GList;
import wraith.GMap;
import wraith.VectorMath;
import wraith.W;
import wraith.Wraith;

public abstract class BaseProvider implements PortalProvider
{
	private RasteredSystem rasterer;
	private GList<Player> moved;
	
	public BaseProvider()
	{
		rasterer = new RasteredSystem();
		moved = new GList<Player>();
	}
	
	@Override
	public void flush()
	{
		onFlush();
	}
	
	public abstract void onFlush();
	
	public GList<Integer> getBase(int max)
	{
		GList<Integer> base = new GList<Integer>();
		max = max % 2 == 0 ? max + 1 : max;
		
		while(max >= 3)
		{
			base.add(max * max);
			max -= 2;
		}
		
		return base;
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
	public void wipe(LocalPortal p)
	{
		if(VP.host.isKeyValidAlready(p.getKey()))
		{
			File f = new File(VP.instance.getDataFolder(), "data");
			f.mkdirs();
			
			for(File i : f.listFiles())
			{
				if(i.getName().endsWith(".k"))
				{
					try
					{
						DataCluster cc = VP.io.load(i);
						World w = Bukkit.getWorld(cc.getString("g"));
						Location a = new Location(w, cc.getInt("a"), cc.getInt("b"), cc.getInt("c"));
						Location b = new Location(w, cc.getInt("d"), cc.getInt("e"), cc.getInt("f"));
						PortalKey k = PortalKey.fromSName(cc.getString("i"));
						
						if(k.equals(p.getKey()) && new Cuboid(a, b).equals(p.getPosition().getPane()))
						{
							i.delete();
						}
					}
					
					catch(Exception e)
					{
						
					}
				}
			}
		}
	}
	
	@Override
	public void save(LocalPortal p)
	{
		if(VP.host.isKeyValidAlready(p.getKey()))
		{
			DataCluster cc = new DataCluster();
			PortalKey key = p.getKey();
			PortalPosition pos = p.getPosition();
			Cuboid c = pos.getPane();
			Direction d = pos.getIdentity().getBack();
			cc.set("a", c.getLowerX());
			cc.set("b", c.getLowerY());
			cc.set("c", c.getLowerZ());
			cc.set("d", c.getUpperX());
			cc.set("e", c.getUpperY());
			cc.set("f", c.getUpperZ());
			cc.set("g", c.getWorld().getName());
			cc.set("h", d.ordinal());
			cc.set("i", key.getSName());
			VP.io.save(cc, new File(new File(VP.instance.getDataFolder(), "data"), UUID.randomUUID().toString() + ".k"));
		}
	}
	
	@Override
	public void loadAllPortals()
	{
		File f = new File(VP.instance.getDataFolder(), "data");
		f.mkdirs();
		
		for(File i : f.listFiles())
		{
			if(i.getName().endsWith(".k"))
			{
				try
				{
					DataCluster cc = VP.io.load(i);
					World w = Bukkit.getWorld(cc.getString("g"));
					Location a = new Location(w, cc.getInt("a"), cc.getInt("b"), cc.getInt("c"));
					Location b = new Location(w, cc.getInt("d"), cc.getInt("e"), cc.getInt("f"));
					Direction d = Direction.values()[cc.getInt("h")];
					PortalKey k = PortalKey.fromSName(cc.getString("i"));
					createPortal(d, new Cuboid(a, b));
					System.out.println("Loading Portal: " + k.getSName().toUpperCase());
					i.delete();
				}
				
				catch(Exception e)
				{
					System.out.println("Failed to load data file: " + i.getPath() + ". Deleting");
					i.delete();
				}
			}
		}
	}
	
	public LocalPortal createPortal(Direction d, Cuboid c) throws InvalidPortalKeyException, InvalidPortalPositionException, DuplicatePortalKeyException
	{
		PortalIdentity id = new PortalIdentity(d, null);
		PortalPosition pp = new PortalPosition(id, c);
		id.setKey(buildKey(pp));
		
		return createPortal(id, pp);
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
				Wraith.callEvent(new WormholeCreateEvent(p));
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
	public boolean hasMoved(Player p)
	{
		return moved.contains(p);
	}
	
	@Override
	public GMap<Player, Viewport> getViewport(Portal portal)
	{
		GMap<Player, Viewport> views = new GMap<Player, Viewport>();
		BoundingBox box = portal.getPosition().getBoundingBox();
		box.flush();
		
		for(Entity i : box.getInside())
		{
			if(i instanceof Player)
			{
				if(portal instanceof LocalPortal)
				{
					if(((LocalPortal) portal).getPosition().getPane().contains(i.getLocation()))
					{
						Player p = (Player) i;
						views.put(p, new NulledViewport(p, portal));
					}
					
					else
					{
						Player p = (Player) i;
						views.put(p, getViewport(p, portal));
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
