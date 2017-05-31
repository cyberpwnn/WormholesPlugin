package com.volmit.wormholes.service;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import com.volmit.wormholes.portal.LocalPortal;
import wraith.GList;
import wraith.GSound;
import wraith.M;
import wraith.MSound;
import wraith.ParticleEffect;
import wraith.TaskLater;
import wraith.VectorMath;

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
	
	public Vector throwBackVector(Entity l, LocalPortal p)
	{
		return p.getThrowDirection(l.getLocation()).toVector().clone().add(VectorMath.reverse(l.getVelocity()));
	}
	
	public void ambient(LocalPortal p)
	{
		new GSound(MSound.PORTAL.bukkitSound(), 0.05f, 0.1f + (float) Math.random() * 0.9f).play(new GList<Block>(p.getPosition().getPane().iterator()).pickRandom().getLocation());
		
		if(M.r(0.08))
		{
			new GSound(MSound.PORTAL_TRAVEL.bukkitSound(), 0.05f, 0.1f + (float) Math.random() * 0.9f).play(new GList<Block>(p.getPosition().getPane().iterator()).pickRandom().getLocation());
		}
	}
	
	public void rise(LocalPortal p)
	{
		Location l = new GList<Block>(p.getPosition().getPane().iterator()).pickRandom().getLocation().clone().add(0.5, 1, 0.5);
		
		if(M.r(0.7))
		{
			l.add(p.getIdentity().getUp().toVector().clone().multiply(Math.random()));
			l.add(p.getIdentity().getDown().toVector().clone().multiply(Math.random()));
			l.add(p.getIdentity().getLeft().toVector().clone().multiply(Math.random()));
			l.add(p.getIdentity().getRight().toVector().clone().multiply(Math.random()));
		}
		
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
		
		for(int i = 0; i < 4; i++)
		{
			Vector vx = new Vector(0, 0, 0);
			
			for(int j = 0; j < 18; j++)
			{
				vx.add(vxz.pickRandom());
			}
			
			ParticleEffect.SUSPENDED_DEPTH.display(vx.clone().normalize(), 1f, l, 32);
			
			if(k > 1)
			{
				ParticleEffect.SUSPENDED_DEPTH.display(vx.clone().normalize(), 3f, l, 32);
				
				if(k > 2)
				{
					ParticleEffect.SUSPENDED_DEPTH.display(vx.clone().normalize(), 5f, l, 32);
					
					if(k > 3)
					{
						ParticleEffect.SUSPENDED_DEPTH.display(vx.clone().normalize(), 7f, l, 32);
					}
				}
			}
		}
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
		
		for(int i = 0; i < 64; i++)
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
		
		for(int i = 0; i < 64; i++)
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
	
	public void visualize(LocalPortal p)
	{
		Location l = p.getPosition().getCenter().clone().add(p.getIdentity().getBack().toVector().clone().multiply(8));
		
		for(int i = 0; i < 15; i++)
		{
			Vector vx = p.getPosition().getIdentity().getFront().toVector();
			
			new TaskLater(i)
			{
				@Override
				public void run()
				{
					ParticleEffect.FIREWORKS_SPARK.display(vx.clone().normalize(), 2.0f, l, 32);
				}
			};
		}
	}
}
