package wraith;

/**
 * Fast access to the scheduler
 * 
 * @author cyberpwn
 */
public abstract class TaskLater implements Runnable
{
	public static int taskx = 0;
	
	/**
	 * Run in the next tick
	 */
	public TaskLater()
	{
		this(0);
	}
	
	/**
	 * Run after a delay
	 * 
	 * @param delay
	 *            the delay in ticks
	 */
	public TaskLater(Integer delay)
	{
		Wraith.scheduleSyncTask(delay, new Runnable()
		{
			@Override
			public void run()
			{
				TaskLater.this.run();
			}
		});
	}
	
	@Override
	public abstract void run();
}
