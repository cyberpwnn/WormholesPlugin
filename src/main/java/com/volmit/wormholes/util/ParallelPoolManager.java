package com.volmit.wormholes.util;

import java.util.Queue;

public class ParallelPoolManager
{
	private QueueMode mode;
	private GList<ParallelThread> threads;
	private int next;
	private int threadCount;
	private Queue<Execution> squeue;
	private String key;
	
	public void syncQueue(Execution e)
	{
		squeue.offer(e);
	}
	
	public void tickSyncQueue()
	{
		while(!squeue.isEmpty())
		{
			squeue.poll().run();
		}
	}
	
	public ParallelPoolManager(String key, int threadCount, QueueMode mode)
	{
		this(threadCount, mode);
		
		this.key = key;
	}
	
	public ParallelPoolManager(int threadCount, QueueMode mode)
	{
		if(threadCount < 1)
		{
			threadCount = 1;
		}
		
		if(threadCount > 4)
		{
			System.out.println("WARNING: HIGH THREAD COUNT FOR CORETICK");
		}
		
		threads = new GList<ParallelThread>();
		this.threadCount = threadCount;
		next = 0;
		this.mode = mode;
		key = "Worker Thread";
	}
	
	public void lock()
	{
		while(getQueueSize() != 0)
		{
			try
			{
				Thread.sleep(1);
			}
			
			catch(InterruptedException e)
			{
				
			}
		}
	}
	
	public void start()
	{
		createThreads(threadCount);
	}
	
	public void shutdown()
	{
		for(ParallelThread i : threads)
		{
			i.interrupt();
		}
	}
	
	public ParallelPoolManager(int threadCount)
	{
		this(threadCount, QueueMode.ROUND_ROBIN);
	}
	
	public void queue(Execution e)
	{
		nextThread().queue(e);
	}
	
	public int getSize()
	{
		return threads.size();
	}
	
	public int getQueueSize()
	{
		int s = 0;
		
		for(ParallelThread i : getThreads())
		{
			s += i.getQueue().size();
		}
		
		return s;
	}
	
	public ParallelThread[] getThreads()
	{
		return threads.toArray(new ParallelThread[threads.size()]);
	}
	
	private ParallelThread nextThread()
	{
		if(threads.size() == 1)
		{
			return threads.get(0);
		}
		
		int id = 0;
		
		switch(mode)
		{
			case ROUND_ROBIN:
				next = (next > threads.size() - 1 ? 0 : next + 1);
				id = next;
			case SMALLEST:
				int min = Integer.MAX_VALUE;
				
				for(ParallelThread i : threads)
				{
					int size = i.getQueue().size();
					
					if(size < min)
					{
						min = size;
						id = i.getInfo().getId();
					}
				}
				
			default:
				break;
		}
		
		return threads.get(id);
	}
	
	private void createThreads(int count)
	{
		for(int i = 0; i < count; i++)
		{
			ParallelThread p = new ParallelThread(key, i);
			p.start();
			threads.add(p);
		}
	}
}
