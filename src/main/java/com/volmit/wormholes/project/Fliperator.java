package com.volmit.wormholes.project;

import java.util.Iterator;

public class Fliperator implements Iterator<Integer>
{
	private int min;
	private int max;
	private boolean ascend;
	private int at;

	public Fliperator(int min, int max, boolean ascend)
	{
		this.max = max;
		this.min = min;
		this.ascend = ascend;
		reset();
	}

	public void reset()
	{
		this.at = ascend ? min - 1 : max + 1;
	}

	@Override
	public boolean hasNext()
	{
		return ascend ? at < max : at > min;
	}

	@Override
	public Integer next()
	{
		at += ascend ? 1 : -1;
		return at;
	}
}
