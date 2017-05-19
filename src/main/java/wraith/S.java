package wraith;

public abstract class S
{
	public S()
	{
		Wraith.poolManager.syncQueue(new Execution()
		{
			@Override
			public void run()
			{
				sync();
			}
		});
	}
	
	public abstract void sync();
}
