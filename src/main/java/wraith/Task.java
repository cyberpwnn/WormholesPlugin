package wraith;

/**
 * Fast access to the scheduler
 * 
 * @author cyberpwn
 */
public abstract class Task implements Runnable
{
	public static int taskx = 0;
	private FinalInteger task;
	private Boolean running;
	
	/**
	 * Create a new repeating task
	 * 
	 * @param interval
	 *            the interval
	 */
	public Task(int interval)
	{
		task = new FinalInteger(0);
		
		task.set(Wraith.scheduleSyncRepeatingTask(0, interval, new Runnable()
		{
			@Override
			public void run()
			{
				Task.this.run();
			}
		}));
	}
	
	@Override
	public abstract void run();
	
	/**
	 * Cancel the task
	 */
	public void cancel()
	{
		running = false;
		Wraith.cancelTask(task.get());
		taskx--;
	}
	
	/**
	 * Is it running?
	 * 
	 * @return true if running
	 */
	public boolean isRunning()
	{
		return running;
	}
}
