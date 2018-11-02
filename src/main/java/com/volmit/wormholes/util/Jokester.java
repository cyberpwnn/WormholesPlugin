package com.volmit.wormholes.util;

import java.util.Calendar;

import org.bukkit.Sound;

public enum Jokester
{
	VALENTINES(ParticleEffect.HEART, ParticleEffect.HEART, new Runnable()
	{
		@Override
		public void run()
		{

		}
	}),
	STPATRICKS(ParticleEffect.VILLAGER_HAPPY, ParticleEffect.VILLAGER_HAPPY, new Runnable()
	{
		@Override
		public void run()
		{

		}
	}),
	INDEPENDANCE(ParticleEffect.FIREWORKS_SPARK, ParticleEffect.FIREWORKS_SPARK, new Runnable()
	{
		@Override
		public void run()
		{

		}
	}),
	NEWYEARS(ParticleEffect.FIREWORKS_SPARK, ParticleEffect.FIREWORKS_SPARK, new Runnable()
	{
		@Override
		public void run()
		{

		}
	}),
	SPECIAL(ParticleEffect.ENCHANTMENT_TABLE, ParticleEffect.CRIT_MAGIC, new Runnable()
	{
		@Override
		public void run()
		{

		}
	}),
	CHRISTMAS(ParticleEffect.SNOWBALL, ParticleEffect.SNOW_SHOVEL, new Runnable()
	{
		@Override
		public void run()
		{

		}
	});

	private ParticleEffect swatch1;
	private ParticleEffect swatch2;

	private Jokester(ParticleEffect swatch1, ParticleEffect swatch2, Runnable cfx)
	{
		this.swatch1 = swatch1;
		this.swatch2 = swatch2;
	}

	public static ParticleEffect swatch1(ParticleEffect p)
	{
		Jokester j = get();

		if(j == null)
		{
			return p;
		}

		return j.swatch1;
	}

	public static ParticleEffect swatch2(ParticleEffect p)
	{
		Jokester j = get();

		if(j == null)
		{
			return p;
		}

		return j.swatch2;
	}

	public static Sound flip(Sound p)
	{
		if(p.equals(MSound.PORTAL.bukkitSound()))
		{
			try
			{
				if(M.r(0.18))
				{
					return Sound.valueOf("BLOCK_END_PORTAL_SPAWN");
				}
			}

			catch(Exception e)
			{

			}
		}

		if(p.equals(MSound.AMBIENCE_THUNDER.bukkitSound()))
		{
			try
			{
				return Sound.valueOf("BLOCK_END_PORTAL_SPAWN");
			}

			catch(Exception e)
			{

			}
		}

		if(p.equals(MSound.ENDERMAN_TELEPORT.bukkitSound()))
		{
			try
			{
				return Sound.valueOf("BLOCK_END_PORTAL_SPAWN");
			}

			catch(Exception e)
			{

			}
		}

		if(p.equals(MSound.BLAZE_HIT.bukkitSound()))
		{
			try
			{
				return Sound.valueOf("BLOCK_END_PORTAL_FRAME_FILL");
			}

			catch(Exception e)
			{

			}
		}

		return p;
	}

	public static Sound sound1(Sound p)
	{
		return flip(p);
	}

	public static Sound sound2(Sound p)
	{
		return flip(p);
	}

	private static Jokester get()
	{
		if(isValentinesDay())
		{
			return Jokester.VALENTINES;
		}

		if(isStPatricksDay())
		{
			return Jokester.STPATRICKS;
		}

		if(isIndependanceDay())
		{
			return INDEPENDANCE;
		}

		if(isNewYearsDay())
		{
			return NEWYEARS;
		}

		if(isSpecialDay())
		{
			return SPECIAL;
		}

		if(isChristmasDay())
		{
			return CHRISTMAS;
		}

		return null;
	}

	private static boolean isValentinesDay()
	{
		return is(Calendar.FEBRUARY, 14);
	}

	private static boolean isStPatricksDay()
	{
		return is(Calendar.MARCH, 17);
	}

	private static boolean isIndependanceDay()
	{
		return is(Calendar.JULY, 4);
	}

	private static boolean isNewYearsDay()
	{
		return is(Calendar.JANUARY, 1);
	}

	private static boolean isSpecialDay()
	{
		return is(Calendar.SEPTEMBER, 27);
	}

	private static boolean isChristmasDay()
	{
		return is(Calendar.DECEMBER, 25);
	}

	private static boolean is(int m, int d)
	{
		return monthIs(m) && dayIs(d);
	}

	private static boolean monthIs(int m)
	{
		return Calendar.getInstance().get(Calendar.MONTH) == m;
	}

	private static boolean dayIs(int d)
	{
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == d;
	}
}
