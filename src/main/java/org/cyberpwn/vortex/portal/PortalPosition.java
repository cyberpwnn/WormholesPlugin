package org.cyberpwn.vortex.portal;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.projection.BoundingBox;
import wraith.Axis;
import wraith.Cuboid;
import wraith.Direction;
import wraith.GList;
import wraith.VectorMath;

public class PortalPosition
{
	private Cuboid pane;
	private Cuboid area;
	private Cuboid frameUp;
	private Cuboid frameDown;
	private Cuboid frameLeft;
	private Cuboid frameRight;
	private GList<Cuboid> frame;
	private Location centerUp;
	private Location centerDown;
	private Location centerLeft;
	private Location centerRight;
	private Location center;
	private Location cornerUL;
	private Location cornerUR;
	private Location cornerDL;
	private Location cornerDR;
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
		boundingBox = new BoundingBox(area);
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
}