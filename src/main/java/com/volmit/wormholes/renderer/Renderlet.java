package com.volmit.wormholes.renderer;

import org.bukkit.Location;

import com.volmit.volume.lang.collections.Callback;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.util.Axis;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Direction;

/**
 * Renderlets can iterate through a cuboid in a direction that starts close to
 * the observer (based on viewport) and finishes furthest away from the
 * viewport.
 *
 * Renderlets can iterate in batches. You can call render(maxIterations) and get
 * back the remainder of iterations it had. If it returns zero, there is more to
 * render. This effectively allows pausing and co-processing.
 *
 * @author cyberpwn
 */
public class Renderlet
{
	private Direction direction;
	private Location initial;
	private int x;
	private int y;
	private int z;
	private int ax;
	private int ay;
	private int az;
	private int mx;
	private int my;
	private int mz;
	private boolean done;

	/**
	 * Begin a new Renderlet task. Computes all of the initial information to begin
	 * rendering in batches
	 *
	 * @param viewport
	 *            the viewport calculated from the last portal.
	 */
	public Renderlet(Cuboid viewport, Direction d, LocalPortal p)
	{
		done = false;
		direction = d;
		x = direction.equals(Direction.W) ? 0 : direction.equals(Direction.E) ? viewport.getSizeX() : 0;
		y = direction.equals(Direction.D) ? 0 : direction.equals(Direction.U) ? viewport.getSizeY() : 0;
		z = direction.equals(Direction.N) ? 0 : direction.equals(Direction.S) ? viewport.getSizeZ() : 0;
		ax = direction.equals(Direction.E) ? 0 : direction.equals(Direction.W) ? viewport.getSizeX() : 0;
		ay = direction.equals(Direction.U) ? 0 : direction.equals(Direction.D) ? viewport.getSizeY() : 0;
		az = direction.equals(Direction.S) ? 0 : direction.equals(Direction.N) ? viewport.getSizeZ() : 0;
		mx = x == 0 ? 1 : -1;
		my = y == 0 ? 1 : -1;
		mz = z == 0 ? 1 : -1;
		initial = viewport.getLowerNE();
	}

	/**
	 * Batch render MAXIMUM blocks calling the location caller for each. Iterates in
	 * the direction the viewport is oriented towards.
	 *
	 * @param maximum
	 *            the maximum amount of blocks to iterate through (resuming from the
	 *            last iteration batch)
	 * @param locationCaller
	 *            the callback for each location.
	 * @return returns the remainder of blocks it did not iterate through.
	 *         Essentially this will always equal zero if there is still more to
	 *         iterate through (think stream.read(byte[]) except it's inverse)
	 */
	public int render(int maximum, Callback<Location> locationCaller)
	{
		int m = maximum;

		virtualX: for(int i = getPrimary(); getPrimaryModifier() == 1 ? i < getPrimaryMax() : i >= getPrimaryMax(); i += getPrimaryModifier())
		{
			for(int j = getSecondary(); getSecondaryModifier() == 1 ? j < getSecondaryMax() : j >= getSecondaryMax(); j += getSecondaryModifier())
			{
				for(int k = getTertiary(); getTertiaryModifier() == 1 ? k < getTertiaryMax() : k >= getTertiaryMax(); k += getTertiaryModifier())
				{
					locationCaller.run(initial.clone().add(x, y, z));
					modTertiary();

					if(--m <= 0)
					{
						break virtualX;
					}
				}

				modSecondary();
			}

			modPrimary();
		}

		if(m > 0)
		{
			done = true;
		}

		return m;
	}

	private int getPrimary()
	{
		return direction.getAxis().equals(Axis.X) ? x : direction.getAxis().equals(Axis.Y) ? y : direction.getAxis().equals(Axis.Z) ? z : 0;
	}

	private void modPrimary()
	{
		switch(direction.getAxis())
		{
			case X:
				x += getPrimaryModifier();
				break;
			case Y:
				y += getPrimaryModifier();
				break;
			case Z:
				z += getPrimaryModifier();
				break;
		}
	}

	private int getPrimaryMax()
	{
		return direction.getAxis().equals(Axis.X) ? ax : direction.getAxis().equals(Axis.Y) ? ay : direction.getAxis().equals(Axis.Z) ? az : 0;
	}

	private int getPrimaryModifier()
	{
		return direction.getAxis().equals(Axis.X) ? mx : direction.getAxis().equals(Axis.Y) ? my : direction.getAxis().equals(Axis.Z) ? mz : 1;
	}

	private int getSecondary()
	{
		return direction.getAxis().equals(Axis.X) ? y : direction.getAxis().equals(Axis.Y) ? z : direction.getAxis().equals(Axis.Z) ? x : 0;
	}

	private void modSecondary()
	{
		switch(direction.getAxis())
		{
			case X:
				y += getSecondaryModifier();
				break;
			case Y:
				z += getSecondaryModifier();
				break;
			case Z:
				x += getSecondaryModifier();
				break;
		}
	}

	private int getSecondaryMax()
	{
		return direction.getAxis().equals(Axis.X) ? ay : direction.getAxis().equals(Axis.Y) ? az : direction.getAxis().equals(Axis.Z) ? ax : 0;
	}

	private int getSecondaryModifier()
	{
		return direction.getAxis().equals(Axis.X) ? my : direction.getAxis().equals(Axis.Y) ? mz : direction.getAxis().equals(Axis.Z) ? mx : 1;
	}

	private int getTertiary()
	{
		return direction.getAxis().equals(Axis.X) ? z : direction.getAxis().equals(Axis.Y) ? x : direction.getAxis().equals(Axis.Z) ? y : 0;
	}

	private void modTertiary()
	{
		switch(direction.getAxis())
		{
			case X:
				z += getTertiaryModifier();
				break;
			case Y:
				x += getTertiaryModifier();
				break;
			case Z:
				y += getTertiaryModifier();
				break;
		}
	}

	private int getTertiaryMax()
	{
		return direction.getAxis().equals(Axis.X) ? az : direction.getAxis().equals(Axis.Y) ? ax : direction.getAxis().equals(Axis.Z) ? ay : 0;
	}

	private int getTertiaryModifier()
	{
		return direction.getAxis().equals(Axis.X) ? mz : direction.getAxis().equals(Axis.Y) ? mx : direction.getAxis().equals(Axis.Z) ? my : 1;
	}

	public boolean isDone()
	{
		return done;
	}
}
