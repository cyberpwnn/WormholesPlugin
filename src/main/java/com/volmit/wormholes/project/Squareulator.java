package com.volmit.wormholes.project;

import java.util.Iterator;

public class Squareulator implements Iterator<Fliperator>
{
	private int min;
	private int max;
	private int at;
	private boolean ascend;
	private Fliperator flipper;

	public Squareulator(int min, int max, boolean ascend, Fliperator flipper)
	{
		this.min = min;
		this.max = max;
		this.ascend = ascend;
		this.flipper = flipper;
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
	public Fliperator next()
	{
		at += ascend ? 1 : -1;
		flipper.reset();
		return flipper;
	}

	public int getAt()
	{
		return at;
	}
}
