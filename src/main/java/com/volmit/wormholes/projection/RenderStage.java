package com.volmit.wormholes.projection;

public class RenderStage 
{
	private int stage;
	private int maxStage;
	
	public RenderStage(int maxStage)
	{
		this.maxStage = maxStage;
		this.stage = 0;
	}
	
	public int getCurrentStage()
	{
		return stage;
	}
	
	public int getMaxStage()
	{
		return maxStage;
	}
	
	public boolean hasNextStage()
	{
		return getCurrentStage() < getMaxStage();
	}
	
	public void pop()
	{
		if(!hasNextStage())
		{
			return;
		}
		
		stage++;
	}
}
