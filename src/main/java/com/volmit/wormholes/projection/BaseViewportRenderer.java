package com.volmit.wormholes.projection;

import org.bukkit.entity.Player;
import com.volmit.wormholes.util.Cuboid;

public class BaseViewportRenderer implements ViewportRenderer
{
	private Viewport viewport;
	private int stage;
	
	public BaseViewportRenderer(Viewport viewport, int stage)
	{
		this.viewport = viewport;
		this.stage = stage;
	}
	
	@Override
	public Player getPlayer()
	{
		return getViewport().getP();
	}
	
	@Override
	public Viewport getViewport()
	{
		return viewport;
	}
	
	@Override
	public int getStage()
	{
		return stage;
	}
	
	@Override
	public int getMaxStage()
	{
		return getProjectionSet().get().size() - 1;
	}
	
	@Override
	public ProjectionSet getProjectionSet()
	{
		return getViewport().getProjectionSet().copy();
	}
	
	@Override
	public ProjectionSet getRenderedStages()
	{
		ProjectionSet s = getProjectionSet();
		s.keep(getStage());
		
		return s;
	}
	
	@Override
	public ProjectionSet getNonRenderedStages()
	{
		ProjectionSet s = getProjectionSet();
		s.remove(getStage());
		
		return s;
	}
	
	@Override
	public Cuboid getProjectionStage(int stage)
	{
		return getProjectionSet().get(stage);
	}
	
	@Override
	public boolean isComplete()
	{
		return getStage() == getMaxStage();
	}
}
