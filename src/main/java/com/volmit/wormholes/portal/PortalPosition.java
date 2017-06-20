package com.volmit.wormholes.portal;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.projection.BoundingBox;
import com.volmit.wormholes.util.Axis;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.RayTrace;
import com.volmit.wormholes.util.VectorMath;

public class PortalPosition
{
	private Cuboid pane;
	private Cuboid ipane;
	private Cuboid area;
	private Cuboid frameUp;
	private Cuboid frameDown;
	private Cuboid frameLeft;
	private Cuboid frameRight;
	private Cuboid iarea;
	private GList<Cuboid> frame;
	private GList<Block> keyset;
	private Location centerUp;
	private Location centerDown;
	private Location centerLeft;
	private Location centerRight;
	private Location center;
	private Location cornerUL;
	private Location cornerUR;
	private Location cornerDL;
	private Location cornerDR;
	private Location corneriUL;
	private Location corneriUR;
	private Location corneriDL;
	private Location corneriDR;
	private PortalIdentity identity;
	private BoundingBox boundingBox;
	
	public PortalPosition(PortalIdentity i, Cuboid p)
	{
		pane = p;
		identity = i;
		frameUp = pane.getFace(i.getUp().f());
		frameDown = pane.getFace(i.getDown().f());
		frameLeft = pane.getFace(i.getLeft().f());
		frameRight = pane.getFace(i.getRight().f());
		frame = new GList<Cuboid>().qadd(frameUp).qadd(frameDown).qadd(frameLeft).qadd(frameRight);
		cornerUL = ul();
		cornerUR = ur();
		cornerDL = bl();
		cornerDR = br();
		center = pane.getCenter();
		centerUp = frameUp.getCenter().clone();
		centerDown = frameDown.getCenter().clone();
		centerLeft = frameLeft.getCenter().clone();
		centerRight = frameRight.getCenter().clone();
		area = new Cuboid(center).e(Axis.X, Settings.PROJECTION_SAMPLE_RADIUS).e(Axis.Y, Settings.PROJECTION_SAMPLE_RADIUS).e(Axis.Z, Settings.PROJECTION_SAMPLE_RADIUS);
		iarea = new Cuboid(center).e(Axis.X, 8).e(Axis.Y, 8).e(Axis.Z, 8);
		boundingBox = new BoundingBox(area);
		keyset = new GList<Block>().qadd(centerUp.getBlock()).qadd(centerDown.getBlock()).qadd(centerLeft.getBlock()).qadd(centerRight.getBlock());
		ipane = new Cuboid(pane);
		
		for(Direction j : Direction.udnews())
		{
			if(identity.getFront().equals(j) || identity.getFront().equals(j.reverse()))
			{
				continue;
			}
			
			ipane = ipane.e(j, -1);
		}
		
		corneriUL = uli();
		corneriUR = uri();
		corneriDL = bli();
		corneriDR = bri();
	}
	
	public Location intersectsv(Location l, Vector next)
	{
		return intersectsv(l, l.clone().add(next));
	}
	
	public boolean intersects(Location a, Location b)
	{
		double distance = a.distance(b);
		boolean[] traces = {false};
		Vector direction = VectorMath.direction(a, b);
		
		new RayTrace(a, direction, distance * 1.2, 0.1)
		{
			@Override
			public void onTrace(Location location)
			{
				if(isInsidePortal(location))
				{
					stop();
					traces[0] = true;
				}
			}
		}.trace();
		
		return traces[0];
	}
	
	public Location intersectsv(Location a, Location b)
	{
		double distance = a.distance(b);
		Location[] traces = {null};
		Vector direction = VectorMath.direction(a, b);
		
		new RayTrace(a, direction, distance * 1.2, 0.1)
		{
			@Override
			public void onTrace(Location location)
			{
				if(isInsidePortal(location))
				{
					stop();
					traces[0] = location.clone();
				}
			}
		}.trace();
		
		return traces[0];
	}
	
	public Block getRandomKeyBlock()
	{
		return keyset.pickRandom();
	}
	
	public boolean intersects(Location l, Vector next)
	{
		return intersects(l, l.clone().add(next));
	}
	
	public boolean isInsidePortal(Location l)
	{
		return getPane().contains(l);
	}
	
	public Cuboid getSideArea(Location l)
	{
		Location from = getCenter();
		Direction d = Direction.getDirection(VectorMath.direction(from, l));
		Cuboid c = new Cuboid(from);
		
		for(Direction i : Direction.udnews())
		{
			if(i.equals(d))
			{
				continue;
			}
			
			c = c.e(i, Settings.PROJECTION_SAMPLE_RADIUS * 4);
		}
		
		return c;
	}
	
	private Location ul()
	{
		for(Block i : new GList<Block>(pane.getFace(identity.getUp().f()).iterator()))
		{
			for(Block j : new GList<Block>(pane.getFace(identity.getLeft().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	private Location ur()
	{
		for(Block i : new GList<Block>(pane.getFace(identity.getUp().f()).iterator()))
		{
			for(Block j : new GList<Block>(pane.getFace(identity.getRight().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	private Location bl()
	{
		for(Block i : new GList<Block>(pane.getFace(identity.getDown().f()).iterator()))
		{
			for(Block j : new GList<Block>(pane.getFace(identity.getLeft().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	private Location br()
	{
		for(Block i : new GList<Block>(pane.getFace(identity.getDown().f()).iterator()))
		{
			for(Block j : new GList<Block>(pane.getFace(identity.getRight().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	private Location uli()
	{
		for(Block i : new GList<Block>(ipane.getFace(identity.getUp().f()).iterator()))
		{
			for(Block j : new GList<Block>(ipane.getFace(identity.getLeft().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	private Location uri()
	{
		for(Block i : new GList<Block>(ipane.getFace(identity.getUp().f()).iterator()))
		{
			for(Block j : new GList<Block>(ipane.getFace(identity.getRight().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	private Location bli()
	{
		for(Block i : new GList<Block>(ipane.getFace(identity.getDown().f()).iterator()))
		{
			for(Block j : new GList<Block>(ipane.getFace(identity.getLeft().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	private Location bri()
	{
		for(Block i : new GList<Block>(ipane.getFace(identity.getDown().f()).iterator()))
		{
			for(Block j : new GList<Block>(ipane.getFace(identity.getRight().f()).iterator()))
			{
				if(i.equals(j))
				{
					return j.getLocation().clone().add(0.5, 0.5, 0.5);
				}
			}
		}
		
		return null;
	}
	
	public Cuboid getOPane()
	{
		return pane.e(getIdentity().getAxis(), 24);
	}
	
	public Cuboid getPane()
	{
		return pane;
	}
	
	public Cuboid getArea()
	{
		return area;
	}
	
	public Cuboid getFrameUp()
	{
		return frameUp;
	}
	
	public Cuboid getFrameDown()
	{
		return frameDown;
	}
	
	public Cuboid getFrameLeft()
	{
		return frameLeft;
	}
	
	public Cuboid getFrameRight()
	{
		return frameRight;
	}
	
	public GList<Cuboid> getFrame()
	{
		return frame;
	}
	
	public Location getCenterUp()
	{
		return centerUp.clone();
	}
	
	public Location getCenterDown()
	{
		return centerDown.clone();
	}
	
	public Location getCenterLeft()
	{
		return centerLeft.clone();
	}
	
	public Location getCenterRight()
	{
		return centerRight.clone();
	}
	
	public Location getCenter()
	{
		return center.clone();
	}
	
	public Location getCornerUL()
	{
		return cornerUL.clone();
	}
	
	public Location getCornerUR()
	{
		return cornerUR.clone();
	}
	
	public Location getCornerDL()
	{
		return cornerDL.clone();
	}
	
	public Location getCornerDR()
	{
		return cornerDR.clone();
	}
	
	public PortalIdentity getIdentity()
	{
		return identity;
	}
	
	public BoundingBox getBoundingBox()
	{
		return boundingBox;
	}
	
	public GList<Block> getKeyBlocks()
	{
		return keyset.copy();
	}
	
	public Cuboid getIpane()
	{
		return ipane;
	}
	
	public GList<Block> getKeyset()
	{
		return keyset;
	}
	
	public Location getCorneriUL()
	{
		return corneriUL;
	}
	
	public Location getCorneriUR()
	{
		return corneriUR;
	}
	
	public Location getCorneriDL()
	{
		return corneriDL;
	}
	
	public Location getCorneriDR()
	{
		return corneriDR;
	}
	
	public Cuboid getIarea()
	{
		return iarea;
	}
}
