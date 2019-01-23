package com.volmit.wormholes.geometry;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.volmit.wormholes.util.lang.FinalBoolean;
import com.volmit.wormholes.util.lang.J;
import com.volmit.wormholes.util.lang.VectorMath;

public abstract class SlowRaycast
{
	private boolean success;
	private boolean successf;

	public SlowRaycast(Location source, Location destination, double jumpSize, int interval)
	{
		successf = false;
		success = true;
		Location cursor = source.clone();
		Vector direction = VectorMath.direction(source, destination).multiply(jumpSize);
		int tj = (int) (cursor.distance(destination) / jumpSize);
		FinalBoolean cancelled = new FinalBoolean(false);
		J.ar(new Runnable()
		{
			@Override
			public void run()
			{
				if(cancelled.get())
				{
					return;
				}

				if(!shouldContinue(cursor))
				{
					onDone();
					if(successf)
					{
						success = true;
						cancelled.set(true);
						return;
					}

					success = false;
					cancelled.set(false);
					return;
				}

				cursor.add(direction);
			}
		}, interval, tj);
	}

	public abstract void onDone();

	public boolean finishSuccess()
	{
		successf = true;
		return false;
	}

	public abstract boolean shouldContinue(Location l);

	public boolean hadSuccess()
	{
		return success;
	}
}
