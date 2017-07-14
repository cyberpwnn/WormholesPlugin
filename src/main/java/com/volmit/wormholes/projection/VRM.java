package com.volmit.wormholes.projection;

import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.util.Execution;

public class VRM implements ViewportRenderer
{
	private ViewportRenderer dialater;
	private ViewportRenderer eroder;
	
	public VRM(ViewportRenderer dialater, ViewportRenderer eroder)
	{
		this.dialater = dialater;
		this.eroder = eroder;
	}
	
	public VRM(VRBuilder builder, Viewport dialate, Viewport erode)
	{
		dialater = builder.build();
		((ViewportRendererPortal) dialater).mode = RenderMode.DIALATE;
		((ViewportRendererPortal) dialater).view = dialate;
		((ViewportRendererPortal) dialater).stage = new RenderStage(dialate.getProjectionSet().get().size());
		
		eroder = builder.build();
		((ViewportRendererPortal) eroder).mode = RenderMode.ERODE;
		((ViewportRendererPortal) eroder).view = erode;
		((ViewportRendererPortal) eroder).stage = new RenderStage(erode.getProjectionSet().get().size());
	}
	
	@Override
	public void render()
	{
		eroder.render();
		dialater.render();
	}
	
	public boolean isComplete()
	{
		return !((ViewportRendererPortal) eroder).stage.hasNextStage() || !((ViewportRendererPortal) dialater).stage.hasNextStage();
	}
	
	public Viewport getDialater()
	{
		return ((ViewportRendererPortal) dialater).view;
	}
	
	public Viewport getEroder()
	{
		return ((ViewportRendererPortal) eroder).view;
	}
	
	public void renderAll()
	{
		if(!isComplete())
		{
			int m = Math.max(((ViewportRendererPortal) dialater).stage.getMaxStage(), ((ViewportRendererPortal) eroder).stage.getMaxStage());
			int c = ((ViewportRendererPortal) dialater).stage.getCurrentStage();
			int s = m - c;
			
			for(int i = 0; i < s; i++)
			{
				Wormholes.pool.queue(new Execution()
				{
					@Override
					public void run()
					{
						eroder.render();
					}
				});
			}
			
			for(int i = 0; i < s; i++)
			{
				Wormholes.pool.queue(new Execution()
				{
					@Override
					public void run()
					{
						dialater.render();
					}
				});
			}
		}
	}
}
