package org.cyberpwn.vortex.service;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.portal.LocalPortal;
import wraith.GList;
import wraith.GSound;
import wraith.M;
import wraith.MSound;
import wraith.ParticleEffect;

public class EffectService
{
	public EffectService()
	{
		
	}
	
	public void push(Entity e, Vector v, LocalPortal p)
	{
		phase(p, e.getLocation().getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
		new GSound(MSound.ENDERMAN_TELEPORT.bukkitSound(), 0.5f, 1.7f + (float) (Math.random() * 0.2)).play(e.getLocation());
		new GSound(MSound.ENDERMAN_TELEPORT.bukkitSound(), 0.5f, 1.5f + (float) (Math.random() * 0.2)).play(e.getLocation());
		new GSound(MSound.ENDERMAN_TELEPORT.bukkitSound(), 0.5f, 1.3f + (float) (Math.random() * 0.2)).play(e.getLocation());
	}
	
	public void throwBack(Entity e, Vector v, LocalPortal p)
	{
		phaseDeny(p, e.getLocation().getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
		e.teleport(e.getLocation().clone().add(v));
		e.setVelocity(v);
		new GSound(MSound.BLAZE_HIT.bukkitSound(), 1f, 1.5f + (float) (Math.random() * 0.2)).play(e.getLocation());
	}
	
	public void phaseDeny(LocalPortal p, Location l)
	{
		GList<Vector> vxz = new GList<Vector>().qadd(p.getIdentity().getUp().toVector()).qadd(p.getIdentity().getDown().toVector()).qadd(p.getIdentity().getLeft().toVector()).qadd(p.getIdentity().getRight().toVector());
		int k = 1;
		
		if(M.r(0.7))
		{
			k++;
			
			if(M.r(0.4))
			{
				k++;
				
				if(M.r(0.2))
				{
					k++;
				}
			}
		}
		for(int i = 0; i < 128; i++)
		{
			Vector vx = new Vector(0, 0, 0);
			
			for(int j = 0; j < 18; j++)
			{
				vx.add(vxz.pickRandom());
			}
			
			ParticleEffect.CRIT.display(vx.clone().normalize(), 0.5f, l, 32);
			
			if(k > 1)
			{
				ParticleEffect.CRIT.display(vx.clone().normalize(), 1f, l, 32);
				
				if(k > 2)
				{
					ParticleEffect.CRIT.display(vx.clone().normalize(), 1.5f, l, 32);
					
					if(k > 3)
					{
						ParticleEffect.CRIT.display(vx.clone().normalize(), 2.0f, l, 32);
					}
				}
			}
		}
	}
	
	public void phase(LocalPortal p, Location l)
	{
		GList<Vector> vxz = new GList<Vector>().qadd(p.getIdentity().getUp().toVector()).qadd(p.getIdentity().getDown().toVector()).qadd(p.getIdentity().getLeft().toVector()).qadd(p.getIdentity().getRight().toVector());
		int k = 1;
		
		if(M.r(0.7))
		{
			k++;
			
			if(M.r(0.4))
			{
				k++;
				
				if(M.r(0.2))
				{
					k++;
				}
			}
		}
		
		for(int i = 0; i < 128; i++)
		{
			Vector vx = new Vector(0, 0, 0);
			
			for(int j = 0; j < 18; j++)
			{
				vx.add(vxz.pickRandom());
			}
			
			ParticleEffect.CRIT_MAGIC.display(vx.clone().normalize(), 0.5f, l, 32);
			
			if(k > 1)
			{
				ParticleEffect.CRIT_MAGIC.display(vx.clone().normalize(), 1f, l, 32);
				
				if(k > 2)
				{
					ParticleEffect.CRIT_MAGIC.display(vx.clone().normalize(), 1.5f, l, 32);
					
					if(k > 3)
					{
						ParticleEffect.CRIT_MAGIC.display(vx.clone().normalize(), 2.0f, l, 32);
					}
				}
			}
		}
	}
}
