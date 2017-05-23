package wraith;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ShockEffect
{
	private Float power;
	
	public ShockEffect(Float power)
	{
		this.power = power;
	}
	
	public void play(Location l, Vector dir)
	{
		int[] k = new int[] {0};
		
		new Task(0)
		{
			@Override
			public void run()
			{
				if(k[0] > 2)
				{
					cancel();
				}
				
				for(int j = 0; j < 1; j++)
				{
					Vector direction = dir.clone();
					Location start = l.clone();
					
					for(float i = 0; i < Math.abs(power); i += 0.43f)
					{
						direction.add(new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(Math.random() * 8 * i));
						Location b = start.clone().add(direction);
						getArm().play(start, b.clone(), (double) 9);
						start = b;
					}
				}
				
				k[0]++;
			}
		};
	}
	
	public LineParticleManipulator getArm()
	{
		return new LineParticleManipulator()
		{
			@Override
			public void play(Location l)
			{
				ParticleEffect.CRIT_MAGIC.display(0, 1, l, 28);
			}
		};
	}
}