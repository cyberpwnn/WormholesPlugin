package com.volmit.wormholes.projection;

import com.volmit.wormholes.util.Cuboid;

public class PartialRenderer
{
	private ProjectionSet queue;
	private ProjectionSet rendered;
	private Viewport view;
	
	public PartialRenderer(Viewport view)
	{
		this.view = view;
		queue = view.getProjectionSet().copy();
		rendered = new ProjectionSet();
	}
	
	public boolean isComplete()
	{
		return queue.get().isEmpty();
	}
	
	public Cuboid next()
	{
		if(isComplete())
		{
			return null;
		}
		
		Cuboid c = queue.pop();
		rendered.add(c);
		
		return c;
	}
	
	public ProjectionSet getQueue()
	{
		return queue;
	}
	
	public ProjectionSet getRendered()
	{
		return rendered;
	}
	
	public Viewport getView()
	{
		return view;
	}
}
