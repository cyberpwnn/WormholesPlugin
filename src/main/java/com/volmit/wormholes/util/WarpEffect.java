package com.volmit.wormholes.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class WarpEffect
{
	private Float power;
	
	public WarpEffect(Float power)
	{
		this.power = power;
	}
	
	public void play(Location l, Vector dir)
	{
		Vector direction = dir.clone();
		Location[] start = {l.clone()};
		int m = 0;
		
		for(int kk = 0; kk < 2; kk++)
		{
			for(float i = 0; i < Math.abs(power); i += 0.19f)
			{
				m += 8;
				int kv = m;
				
				new TaskLater(m)
				{
					@Override
					public void run()
					{
						direction.add(new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(2));
						Location b = start[0].clone().add(direction);
						getArm(kv).play(start[0], b.clone(), (double) 14);
						start[0] = b;
					}
				};
			}
		}
	}
	
	public LineParticleManipulator getArm(int m)
	{
		int[] kv = {0};
		
		return new LineParticleManipulator()
		{
			@Override
			public void play(Location l)
			{
				kv[0]++;
				new TaskLater(kv[0] + m)
				{
					@Override
					public void run()
					{
						ParticleEffect.SUSPENDED_DEPTH.display(0, 1, l, 28);
					}
				};
			}
		};
	}
}
