package com.volmit.wormholes.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import com.volmit.wormholes.Settings;

public class ShockEffect
{
	private Float power;
	
	public ShockEffect(Float power)
	{
		this.power = power;
	}
	
	public void play(Location l, Vector dir)
	{
		Vector direction = dir.clone();
		Location start = l.clone();
		
		for(float i = 0; i < Math.abs(power); i += 0.19f)
		{
			direction.add(new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(Math.random() * 3 * i));
			Location b = start.clone().add(direction);
			getArm().play(start, b.clone(), (double) 14);
			start = b;
		}
	}
	
	public LineParticleManipulator getArm()
	{
		return new LineParticleManipulator()
		{
			@Override
			public void play(Location l)
			{
				Jokester.swatch1(Settings.getLightningParticle()).display(0, 1, l, 28);
			}
		};
	}
}
