package com.volmit.wormholes.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParallelThread extends Thread
{
	private Queue<Execution> queue;
	private ThreadInformation info;
	private boolean working;

	public ParallelThread(String key, int id)
	{
		queue = new ConcurrentLinkedQueue<Execution>();
		info = new ThreadInformation(id);
		setPriority(MAX_PRIORITY);
		setName("Wormhole " + key + " " + id);
		working = false;
	}

	@Override
	public void run()
	{
		while(!interrupted())
		{
			try
			{
				double time = 0;
				double diff = 0;
				double tdiff = 0;
				long ns = System.nanoTime();
				info.setProcessing(true);
				info.setQueuedSize(queue.size());
				execute();
				time = (double) (System.nanoTime() - ns) / 1000000.0;
				diff = (50.0 - time) < 0 ? 0 : 50.0 - time;
				tdiff = (long) (time + diff);
				info.setProcessing(false);
				conditionallySleep(diff);
				info.setTicksPerSecond(20 - ((1.0 - (50.0 / tdiff)) * 20));
				info.setTicksPerSecond(info.getTicksPerSecond() < 0 ? 0 : info.getTicksPerSecond());
				info.setUtilization(1.0 - (info.getTicksPerSecond() / 20.0));
				info.setTick(info.getTick() + 1);
			}

			catch(InterruptedException e)
			{
				working = false;
				System.out.println("Shutting Down " + getName());
			}

			catch(Exception e)
			{
				working = false;
				e.printStackTrace();
			}
		}

		working = false;
	}

	private void conditionallySleep(double diff) throws InterruptedException
	{
		if(info.getTick() >= TICK.tick)
		{
			Thread.sleep((long) diff);
		}
	}

	private void execute(Execution e)
	{
		try
		{
			e.run();
		}

		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void execute()
	{
		working = true;
		while(!queue.isEmpty())
		{
			if(interrupted())
			{
				working = false;
				System.out.println("Parallel Thread " + info.getId() + " Interrupted mid-execution");
				return;
			}

			execute(queue.poll());
		}
		working = false;
	}

	public void queue(Execution e)
	{
		queue.offer(e);
	}

	public Queue<Execution> getQueue()
	{
		return queue;
	}

	public ThreadInformation getInfo()
	{
		return info;
	}

	public boolean isWorking()
	{
		return working;
	}
}
