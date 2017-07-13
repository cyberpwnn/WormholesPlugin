package com.volmit.wormholes.projection;

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
		this.dialater = builder.build();
		((ViewportRendererPortal)dialater).mode = RenderMode.DIALATE;
		((ViewportRendererPortal)dialater).view = dialate;
		((ViewportRendererPortal)dialater).stage = new RenderStage(dialate.getProjectionSet().get().size());
		
		this.eroder = builder.build();
		((ViewportRendererPortal)eroder).mode = RenderMode.ERODE;
		((ViewportRendererPortal)eroder).view = erode;
		((ViewportRendererPortal)eroder).stage = new RenderStage(erode.getProjectionSet().get().size());
	}
	
	public void render()
	{
		eroder.render();
		dialater.render();
	}
	
	public boolean isComplete()
	{
		return !((ViewportRendererPortal)eroder).stage.hasNextStage() || !((ViewportRendererPortal)dialater).stage.hasNextStage();
	}
	
	public Viewport getDialater()
	{
		return ((ViewportRendererPortal)dialater).view;
	}
	
	public Viewport getEroder()
	{
		return ((ViewportRendererPortal)eroder).view;
	}
}
