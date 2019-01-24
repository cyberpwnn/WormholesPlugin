package com.volmit.wormholes.project;

import org.bukkit.util.Vector;

import com.volmit.wormholes.util.Axis;
import com.volmit.wormholes.util.AxisAlignedBB;
import com.volmit.wormholes.util.Direction;

public abstract class DirectionalIterator
{
	private Cubulator c;
	private boolean done;
	private Axis[] axes;
	private int va;
	private int vb;
	private int vc;
	private int v1;
	private int v2;
	private int v3;

	public DirectionalIterator(AxisAlignedBB bb, Direction d)
	{
		done = false;
		Vector min = bb.min();
		Vector max = bb.max();
		axes = new Axis[3];

		//@builder
		switch(d)
		{
			case D:
				axes = new Axis[] {Axis.Y, Axis.X, Axis.Z};
				c = new Cubulator(min.getBlockY(), max.getBlockY(), false,
						new Squareulator(min.getBlockX(), max.getBlockX(), true,
								new Fliperator(min.getBlockZ(), max.getBlockZ(), true)));
				break;
			case E:
				axes = new Axis[] {Axis.X, Axis.Y, Axis.Z};
				c = new Cubulator(min.getBlockX(), max.getBlockX(), true,
						new Squareulator(min.getBlockY(), max.getBlockY(), true,
								new Fliperator(min.getBlockZ(), max.getBlockZ(), true)));
				break;
			case N:
				axes = new Axis[] {Axis.Z, Axis.Y, Axis.X};
				c = new Cubulator(min.getBlockZ(), max.getBlockZ(), false,
						new Squareulator(min.getBlockY(), max.getBlockY(), true,
								new Fliperator(min.getBlockX(), max.getBlockX(), true)));
				break;
			case S:
				axes = new Axis[] {Axis.Z, Axis.Y, Axis.X};
				c = new Cubulator(min.getBlockZ(), max.getBlockZ(), true,
						new Squareulator(min.getBlockY(), max.getBlockY(), true,
								new Fliperator(min.getBlockX(), max.getBlockX(), true)));
				break;
			case U:
				axes = new Axis[] {Axis.Y, Axis.X, Axis.Z};
				c = new Cubulator(min.getBlockY(), max.getBlockY(), true,
						new Squareulator(min.getBlockX(), max.getBlockX(), true,
								new Fliperator(min.getBlockZ(), max.getBlockZ(), true)));
				break;
			case W:
				axes = new Axis[] {Axis.X, Axis.Y, Axis.Z};
				c = new Cubulator(min.getBlockX(), max.getBlockX(), false,
						new Squareulator(min.getBlockY(), max.getBlockY(), true,
								new Fliperator(min.getBlockZ(), max.getBlockZ(), true)));
				break;
		}
		//@done
	}

	public void computeNextSection()
	{
		va = -1;
		while(!done && va >> 4 == (va >> 4) << 4)
		{
			computeNextLayer();
		}
	}

	public void computeNextLayer()
	{
		if(done)
		{
			return;
		}

		if(c.hasNext())
		{
			Squareulator s = c.next();
			va = c.getAt();

			while(s.hasNext())
			{
				Fliperator f = s.next();
				vb = s.getAt();

				while(f.hasNext())
				{
					vc = f.next();
					v1 = axes[0].equals(Axis.X) ? va : axes[1].equals(Axis.X) ? vb : vc;
					v2 = axes[0].equals(Axis.Y) ? va : axes[1].equals(Axis.Y) ? vb : vc;
					v3 = axes[0].equals(Axis.Z) ? va : axes[1].equals(Axis.Z) ? vb : vc;
					process(v1, v2, v3);
				}
			}
		}

		else
		{
			done = true;
		}
	}

	public abstract void process(int x, int y, int z);
}
