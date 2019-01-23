package com.volmit.wormholes.util;

import java.io.File;
import java.io.IOException;

import com.volmit.volume.bukkit.task.A;
import com.volmit.volume.bukkit.task.S;

public abstract class Controller implements Controllable
{
	protected Controllable parent;
	protected GList<Controllable> children;
	protected String name;
	protected boolean active;
	protected boolean root;
	protected boolean ticked;
	protected double tickRate;
	protected TickHandler tickHandle;
	protected Task task;
	protected DB d;

	public Controller(Controllable parent)
	{
		this.parent = parent;
		children = new GList<Controllable>();
		name = getClass().getSimpleName();
		active = false;
		root = parent == null;
		ticked = false;
		tickRate = 0;
		tickHandle = TickHandler.SYNCED;
		d = new DB(getName());
		preStart();
	}

	public void i(String... s)
	{
		d.i(s);
	}

	public void s(String... o)
	{
		d.s(o);
	}

	public void f(String... o)
	{
		d.f(o);
	}

	public void w(String... o)
	{
		d.w(o);
	}

	public void v(String... o)
	{
		d.v(o);
	}

	public void o(String... o)
	{
		d.o(o);
	}

	/**
	 * Load a data cluster from file This will also create the file and add in
	 * default values if it doesnt exist
	 *
	 * @param c
	 *            the configurable object
	 */
	public void loadCluster(Configurable c)
	{
		loadCluster(c, null);
	}

	public void saveCluster(Configurable c)
	{
		saveCluster(c, null);
	}

	public void saveCluster(Configurable c, String category)
	{
		File base = Wraith.instance.getDataFolder();

		if(category != null)
		{
			base = new File(base, category);
		}

		try
		{
			File b = base;

			try
			{
				ConfigurationHandler.save(b, c);
			}

			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Load a data cluster from file This will also create the file and add in
	 * default values if it doesnt exist
	 *
	 * @param c
	 *            the configurable object
	 * @param category
	 *            the category
	 */
	public void loadCluster(Configurable c, String category)
	{
		File base = Wraith.instance.getDataFolder();

		if(category != null)
		{
			base = new File(base, category);
		}

		try
		{
			if(c.getClass().isAnnotationPresent(AsyncConfig.class))
			{
				final File abase = base;
				v("@Async Loading " + c.getCodeName());

				new A()
				{
					@Override
					public void run()
					{
						try
						{
							ConfigurationHandler.read(abase, c);
						}

						catch(IOException e)
						{
							e.printStackTrace();
						}
					}
				};
			}

			else
			{
				File b = base;

				new S()
				{
					@Override
					public void run()
					{
						try
						{
							ConfigurationHandler.read(b, c);
						}

						catch(IOException e)
						{
							e.printStackTrace();
						}
					}
				};
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void preStart()
	{
		if(findAutoRegister() && !isRoot())
		{
			parent.register(this);
		}

		if(findTicked())
		{
			ticked = true;
			tickRate = findTickValue();
			tickHandle = findTickHandle();
		}
	}

	private boolean findTicked()
	{
		return getClass().isAnnotationPresent(Ticked.class);
	}

	private double findTickValue()
	{
		if(ticked)
		{
			return getClass().getAnnotationsByType(Ticked.class)[0].value();
		}

		return 0;
	}

	private TickHandler findTickHandle()
	{
		if(getClass().isAnnotationPresent(TickHandle.class))
		{
			return getClass().getAnnotationsByType(TickHandle.class)[0].value();
		}

		return TickHandler.SYNCED;
	}

	private boolean findAutoRegister()
	{
		if(getClass().isAnnotationPresent(Registrar.class))
		{
			RegistrarType rt = getClass().getAnnotationsByType(Registrar.class)[0].value();

			return rt.equals(RegistrarType.AUTO);
		}

		return true;
	}

	private void activate()
	{
		if(isTicked())
		{
			switch(tickHandle)
			{
				case REALTIME:
					task = new Task(0)
					{
						@Override
						public void run()
						{
							long ns = M.ns();
							long lastTime = 0;

							while(M.ns() - ns < (tickRate * 1000000) - lastTime)
							{
								Timer t = new Timer();
								t.start();
								tick();
								t.stop();
								lastTime = t.getTime();
							}
						}
					};
				case SYNCED:
					task = new Task((int) tickRate)
					{
						@Override
						public void run()
						{
							tick();
						}
					};
			}
		}

		Wraith.registerListener(this);
	}

	private void deactivate()
	{
		Wraith.unregisterListener(this);
	}

	@Override
	public void tick()
	{
		if(isActive() && isTicked())
		{
			onTick();
		}
	}

	@Override
	public void start()
	{
		if(!isActive())
		{
			for(Controllable i : getChildren())
			{
				i.start();
			}

			onStart();
			activate();
			active = true;
		}
	}

	@Override
	public void stop()
	{
		if(isActive())
		{
			deactivate();

			for(Controllable i : getChildren())
			{
				i.stop();
			}

			onStop();
			active = false;
		}
	}

	public void onTick()
	{

	}

	public abstract void onStart();

	public abstract void onStop();

	@Override
	public Controllable getParent()
	{
		return parent;
	}

	@Override
	public GList<Controllable> getChildren()
	{
		return children.copy();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isRoot()
	{
		return root;
	}

	@Override
	public boolean isActive()
	{
		return active;
	}

	@Override
	public void register(Controllable controllable)
	{
		children.add(controllable);
	}

	@Override
	public boolean isTicked()
	{
		return ticked;
	}

	@Override
	public double getTickRate()
	{
		return tickRate;
	}

	@Override
	public TickHandler getTickHandler()
	{
		return tickHandle;
	}
}
