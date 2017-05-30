package org.cyberpwn.vortex.projection;

public abstract class QueuedChunk
{
	private int bytes;
	private int dist;
	
	public abstract void run();
	
	public QueuedChunk(int bytes, int dist)
	{
		this.bytes = bytes;
		this.dist = dist;
	}
	
	public int getBytes()
	{
		return bytes;
	}
	
	public int getDist()
	{
		return dist;
	}
}
