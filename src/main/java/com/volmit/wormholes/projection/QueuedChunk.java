package com.volmit.wormholes.projection;

public abstract class QueuedChunk
{
	private int bytes;
	private int dist;
	private int lf;
	
	public abstract void run();
	
	public QueuedChunk(int bytes, int dist, int lf)
	{
		this.bytes = bytes;
		this.dist = dist;
		this.lf = lf;
	}
	
	public int getBytes()
	{
		return bytes;
	}
	
	public int getDist()
	{
		return dist;
	}
	
	public int getLf()
	{
		return lf;
	}
}
