package wraith;

public abstract class SR<T>
{
	private T t = null;
	private boolean f = false;
	
	public SR()
	{
		Wraith.poolManager.syncQueue(new Execution()
		{
			@Override
			public void run()
			{
				t = sync();
				f = true;
			}
		});
		
		while(!f)
		{
			try
			{
				Thread.sleep(5);
			}
			
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public T get()
	{
		return t;
	}
	
	public abstract T sync();
}
