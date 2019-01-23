package com.volmit.wormholes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.volmit.volume.bukkit.VolumePlugin;
import com.volmit.volume.bukkit.task.A;
import com.volmit.volume.bukkit.task.AR;
import com.volmit.volume.bukkit.task.S;
import com.volmit.volume.bukkit.task.SR;
import com.volmit.volume.bukkit.task.TaskManager;

public class WTaskManager extends TaskManager
{
	private ExecutorService es;

	public WTaskManager(VolumePlugin p, String name, int tc)
	{
		super(p);
		A.m = this;
		AR.m = this;
		S.m = this;
		SR.m = this;
		System.out.println("Im ALIVE");

		final ForkJoinWorkerThreadFactory factory = new ForkJoinWorkerThreadFactory()
		{
			@Override
			public ForkJoinWorkerThread newThread(ForkJoinPool pool)
			{
				final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
				worker.setName(name + (worker.getPoolIndex() + 1));
				return worker;
			}
		};

		es = new ForkJoinPool(tc, factory, null, false);
	}

	@Override
	public void shutDown()
	{
		es.shutdown();

		try
		{
			es.awaitTermination(3, TimeUnit.SECONDS);
		}

		catch(InterruptedException e)
		{
			System.out.println("Previous wormholes thread pool did not shutdown after waiting for 3 seconds. It will shut down eventually, but we cant keep waiting.");
		}
	}

	@Override
	public int async(Runnable r)
	{
		try
		{
			es.submit(r);
		}

		catch(RejectedExecutionException e)
		{
			// :(

			new S(1)
			{
				@Override
				public void run()
				{
					es.submit(r);
				}
			};
		}

		return 0;
	}
}
